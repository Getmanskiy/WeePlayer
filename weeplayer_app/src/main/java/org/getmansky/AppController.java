package org.getmansky;

import com.omniscient.log4jcontrib.swingappender.ui.SwingAppenderUI;
import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.Provider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.swing.KeyStroke;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.getmansky.model.Playlist;
import org.getmansky.model.Track;
import org.getmansky.util.Tween;

public class AppController implements Initializable {

   private final static Logger log = Logger.getLogger(AppController.class);
   private ResourceBundle res = App.res;
   private static AppController instance;
   
   private static MediaPlayer player;
   private boolean isPlaying = false;
   private boolean interfaceDisabled = false;
   private boolean searching = false;
   private boolean tweenBlock = false;
   private Playlist currentPlaylist;
   private Playlist rememberedPlaylist;
   private Track currentTrack;
   private Deque<PlaylistAndTrack> playStack = new LinkedList<>();
   
   private final Provider hotkeysProvider = Provider.getCurrentProvider(true);
   private boolean numpadOff = false;
   
   InputStream currentAudioIn;
   
   private Node imagePlay;
   private Node imagePause;
   private Node imageSettings;
   private Node imageUpdate;
   
   private Node imageAdd;
   private Node imageRename;
   private Node imageDelete;
   
   private final ContextMenu playlistsContextMenu = new ContextMenu();
   private final ContextMenu tracksContextMenu = new ContextMenu();
   
   @FXML private TableView playlistsView;
   @FXML private TableView tracksView;
   @FXML private Button stateButton;
   @FXML private Label titleLabel;
   @FXML private Label timeLabel;
   @FXML private ProgressBar loadProgressBar;
   @FXML private Label volumeLabel;
   @FXML private Slider volumeSlider;
   @FXML private ChoiceBox modeList;
   @FXML private Label infoLabel;
   @FXML private TextField searchText;
   @FXML private Button searchButton;
   @FXML private Button settingsButton;
   @FXML private Button refreshButton;
   @FXML private Button addButton;
   @FXML private Button renameButton;
   @FXML private Button deleteButton;
   
   private Stage stage;
   
   void fillTracks(List<Track> tracks) {
      ObservableList<Track> items = FXCollections.observableArrayList(tracks);
      tracksView.setItems(items);
   }
   
   @FXML
   void toggleState() {
      if(tweenBlock) return;
      tweenBlock = true;
      if(isPlaying) {
         stateButton.setGraphic(imagePlay);
         pause(()->{ tweenBlock = false; });
      } else {
	 if(currentTrack != null) {
	    stateButton.setGraphic(imagePause);
	    resume(()->{ tweenBlock = false; });
	 }
      }
   }
   
   @FXML
   void seek(MouseEvent evt) {
      if(player != null) {
         Duration d = player.getMedia().getDuration();
         double millis = (evt.getX() / loadProgressBar.getWidth()) * d.toMillis();
         Duration s = new Duration(millis);
         player.seek(s);
      }
   }
   
   void stop(Runnable callback) {
      if (player != null) {
	 new Tween(player.getVolume()).tweenToZero(0.06, 10L, (curVolume) -> {
	    player.setVolume(curVolume);
	 }, (zeroVolume) -> {
	    isPlaying = false;
	    try {
	       if (currentAudioIn != null) {
		  currentAudioIn.close();
	       }
	       player.stop();
	       callback.run();
	    } catch (IOException | IllegalStateException | NullPointerException ex) {
	       log.log(Level.ERROR, null, ex);
	    }
	 });
      } else {
	 callback.run();
      }
   }
   
   void pause(Runnable callback) {
      if(player != null) {
	 new Tween(player.getVolume()).tweenToZero(0.06, 10L, (curVolume)->{
	    player.setVolume(curVolume);
	 }, (zeroVolume)->{
	    player.pause();
	    callback.run();
	 });
         isPlaying = false;
      } else {
	 callback.run();
      }
   }
   
   void resume(Runnable callback) {
      if(player != null) {
	 player.setVolume(0d);
	 player.play();
	 new Tween(player.getVolume()).tweenToValue(Settings.currentVolume, 0.06, 10L, (currentVolume)->{
	    player.setVolume(currentVolume);
	 }, (valueVolume)->{
	    player.setVolume(valueVolume);
	    callback.run();
	 });
	 isPlaying = true;
      } else {
	 callback.run();
      }
   }
   
   void playNext() {
      List<Track> tracks = tracksView.getItems();
      int selected = tracksView.getSelectionModel().getSelectedIndex();
      int next = selected + 1;
      if (next >= tracks.size()) {
         next = 0;
      }
      tracksView.getSelectionModel().select(next);
      tracksView.scrollTo(tracksView.getSelectionModel().getSelectedIndex());
      Track nextTrack = (Track) tracksView.getSelectionModel().getSelectedItem();
      playTrack(nextTrack);
   }
   
   void pushToPlayStack(Playlist p, Track t) {
      if(playStack.size() > 10000) {
	 playStack.removeLast();
      }
      playStack.push(new PlaylistAndTrack(p, t));
   }
   
   void playSame() {
      player.seek(Duration.ZERO);
      player.play();
   }
   
   void playRandom() {
      List<Track> tracks = tracksView.getItems();
      Random random = new Random();
      int size = tracks.size();
      int r = random.nextInt(size - 1);
      tracksView.getSelectionModel().select(r);
      tracksView.scrollTo(tracksView.getSelectionModel().getSelectedIndex());
      Track nextTrack = (Track) tracksView.getSelectionModel().getSelectedItem();
      playTrack(nextTrack);
   }
   
   void playPrevious() {
      if(playStack.size() < 2) return;
      System.out.println("------------");
      for(PlaylistAndTrack pnt : playStack) {
	 System.out.println(pnt.track.getTitle());
      }
      playStack.removeFirst();
      PlaylistAndTrack prevPlaylistAndTrack = playStack.removeFirst();
      playlistsView.getSelectionModel().select(prevPlaylistAndTrack.playlist);
      tracksView.getSelectionModel().select(prevPlaylistAndTrack.track);
      tracksView.scrollTo(tracksView.getSelectionModel().getSelectedIndex());
      playTrack(prevPlaylistAndTrack.track);
   }
   
   int timeUpdate = 0;
   void updatePlayProgress() {
      if(player != null && Tray.stage.isShowing()) {
         Duration d = player.getMedia().getDuration();
         Duration t = player.getCurrentTime();
         loadProgressBar.setProgress(t.toMillis() / d.toMillis());
         timeUpdate++;
         if(timeUpdate == 10) { 
            timeUpdate = 0;
            Platform.runLater(() -> {
               timeLabel.setText(formatTime(d,t));
            });
         }
      }
   }
   
   void updatePlaylists() {
      if(interfaceDisabled) return;
      final ObservableList<Playlist> items = playlistsView.getItems();
      items.clear();
      items.addAll(Cache.playlists());
      if(Settings.rememberedPlaylistId != null)
	 for(Playlist p : items) {
	    if(p.getId().equals(Settings.rememberedPlaylistId)) {
	       rememberedPlaylist = p;
	       break;
	    }
	 }
   }
   
   private final Label offlinePlaceHolder = new Label(res.getString("empty_playlist_placeholder_offline"));
   private final Label searchPlaceholder = new Label(res.getString("no_results_found"));
   private final Label playlistPlaceholder = new Label(res.getString("playlist_placeholder"));
   
   private boolean findTrack = false;
   @FXML
   void loadSelectedPlaylist() {
      Playlist p = (Playlist) playlistsView.getSelectionModel().getSelectedItem();
      if(p == null) return;
      tracksView.setPlaceholder(offlinePlaceHolder);
      Platform.runLater(()->{
	 List<Track> tracks = p.getTracks();
	 tracksView.getItems().clear();
	 for (int i = tracks.size() - 1; i >= 0; i--) {
	    tracksView.getItems().add(tracks.get(i));
	 }
	 if (findTrack) {
	    Platform.runLater(()->{
	       tracksView.getSelectionModel().select(currentTrack);
	       tracksView.scrollTo(currentTrack);
	       tracksView.requestFocus();
	       findTrack = false;
	    });
	 } else {
	    if (tracksView.getItems().size() > 0) {
	       tracksView.scrollTo(0);
	    }
	 }
      });
   }
   
   @FXML
   void playTrackFromKeyboard(KeyEvent e) {
      if(e.getCode() == KeyCode.ENTER) {
         playSelectedTrack();
         e.consume();
      }
   }
   
   @FXML
   void playTrack(Track t) {
      if(tweenBlock) return;
      tweenBlock = true;
      stop(()->{
	 currentPlaylist = (Playlist) playlistsView.getSelectionModel().getSelectedItem();
	 currentTrack = t;
	 if (player != null) {
	    player.dispose();
	 }
	 if (t == null) {
	    return;
	 }
	 timeUpdate = 9;
	 if (currentPlaylist != null) {
	    Tray.announce("[" + currentPlaylist.getTitle() + "] " + t.getTitle());
	 }

	 Platform.runLater(() -> {
	    infoLabel.setText(res.getString("playing_offline") + t.getTitle());
	    log.info("Playing offline: " + t.getTitle());
	 });
	 try {
	    Media media = new Media(Cache.getContent(t).toURI().toString());
	    player = new MediaPlayer(media);
	    player.setVolume(Settings.currentVolume);
	    player.play();
	 } catch (MediaException ex) {
	    Platform.runLater(() -> {
	       infoLabel.setText(String.format(res.getString("cant_play_offline_not_exist"), t.getTitle()));
	       log.error("Can't play: seems that track does not exist - " + t.getTitle());
	    });
	 }

	 if (player != null) {
	    pushToPlayStack(getSelectedPlaylist(), t);
	    player.setOnEndOfMedia(() -> {
	       String mode = Settings.currentMode;
	       if (mode.equals(Settings.MODE_NEXT)) {
		  playNext();
	       } else if (mode.equals(Settings.MODE_RANDOM)) {
		  playRandom();
	       } else if (mode.equals(Settings.MODE_SAME)) {
		  playSame();
	       }
	    });
	 }

	 Platform.runLater(() -> {
	    isPlaying = true;
	    tweenBlock = false;
	    titleLabel.setText(t.getTitle());
	    stateButton.setGraphic(imagePause);
	 });
      });
   }
   
   @FXML
   void search() {
      searching = true;
      new Thread(() -> {
	 List<Track> tracks = Cache.search(searchText.getText());
	 tracksView.setPlaceholder(searchPlaceholder);
	 Platform.runLater(() -> {
	    if (tracksView.getItems().size() > 0) {
	       tracksView.scrollTo(0);
	    }
	    tracksView.getItems().clear();
	    tracksView.getItems().addAll(tracks.toArray());
	 });
      }).start();
   }
   
   @FXML
   void refreshPlaylists() {
      infoLabel.setText(res.getString("refreshing"));
      log.info("Refreshing playlists");

      ObservableList<Playlist> items = playlistsView.getItems();
      items.clear();
      items.addAll(Cache.playlists());

      if(Settings.rememberedPlaylistId != null)
	 for(Playlist p : items) {
	    if(p.getId().equals(Settings.rememberedPlaylistId)) {
	       rememberedPlaylist = p;
	       break;
	    }
	 }

      infoLabel.setText(res.getString("playlists_refreshed"));
      log.info("Playlists refreshed");
   }
   
   private void setupPlaylistsView() {
      TableColumn titleColumn = new TableColumn("Title");
      titleColumn.setMinWidth(USE_COMPUTED_SIZE);
      titleColumn.setSortable(false);
      titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

      TableColumn countColumn = new TableColumn("Tracks");
      countColumn.setMinWidth(60);
      countColumn.setMaxWidth(60);
      countColumn.setSortable(false);
      countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
      countColumn.getStyleClass().add("text-layout-right");
      
      playlistsView.getColumns().clear();
      playlistsView.getColumns().addAll(titleColumn, countColumn);
      playlistsView.setContextMenu(playlistsContextMenu);
      playlistsView.setPlaceholder(playlistPlaceholder);
      playlistsView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
   }
   
   private void setupTracksView() {
      TableColumn titleColumn = new TableColumn("Title");
      titleColumn.setSortable(false);
      titleColumn.setMinWidth(USE_COMPUTED_SIZE);
      titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
      
      TableColumn durationColumn = new TableColumn("Duration");
      durationColumn.setMinWidth(60);
      durationColumn.setMaxWidth(60);
      durationColumn.setSortable(false);
      durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationFormatted"));
      durationColumn.getStyleClass().add("text-layout-center");
      
      tracksView.getColumns().clear();
      tracksView.getColumns().addAll(titleColumn, durationColumn);
      tracksView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
   }
   
   public void onShown() {
      hideTablesHeaders();
   }
   
   private void hideTablesHeaders() {
      List<Pane> headers = Arrays.asList((Pane)tracksView.lookup("TableHeaderRow"), (Pane)playlistsView.lookup("TableHeaderRow"));
      headers.forEach(header -> {
	 header.setMaxHeight(0);
	 header.setMinHeight(0);
	 header.setPrefHeight(0);
	 header.setVisible(false);
      });
   }
   
   @Override
   public void initialize(URL location, ResourceBundle resources) {
      AppController.instance = this;
      this.res = resources;
      
      ObservableList<String> modeItems = FXCollections.observableArrayList(Settings.MODE_NEXT, Settings.MODE_RANDOM, Settings.MODE_SAME);
      modeList.setItems(modeItems);
      modeList.getSelectionModel().select(Settings.currentMode);
      modeList.valueProperty().addListener((ObservableValue ov, Object oldVal, Object newVal) -> {
         Settings.currentMode = (String) newVal;
      });
      
      setupPlaylistsView();
      setupPlaylistsContextMenu();
      
      setupTracksView();
      
      tracksView.setContextMenu(tracksContextMenu);
      tracksView.setOnContextMenuRequested((ContextMenuEvent evt)->{
         setupTracksContextMenu();
         evt.consume();
      });
      tracksView.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
         Settings.lastTrackId = newValue != null ? ((Track) newValue).getId() : null;
      });
      
      playlistsView.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
         loadSelectedPlaylist();
	 Settings.lastPlaylistId = newValue != null ? ((Playlist) newValue).getId() : null;
	 searching = false;
	 searchText.setText(StringUtils.EMPTY);
      });
      
      volumeSlider.setCursor(Cursor.HAND);
      volumeSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number oldVal, Number newVal) -> {
         if(player != null) {
            player.setVolume(newVal.doubleValue());
         }
         Settings.currentVolume = newVal.doubleValue();
         volumeLabel.setText(String.valueOf((int)Math.ceil(newVal.doubleValue() * 100))+"%");
      });
      volumeSlider.setValue(Settings.currentVolume);
      
      imagePlay = new ImageView(new Image(getClass().getResourceAsStream("/images/button_play.png")));
      imagePlay.setScaleX(0.40);
      imagePlay.setScaleY(0.40);
      imagePause = new ImageView(new Image(getClass().getResourceAsStream("/images/button_pause.png")));
      imagePause.setScaleX(0.5);
      imagePause.setScaleY(0.5);
      imageSettings = new ImageView(new Image(getClass().getResourceAsStream("/images/settings.png")));
      imageSettings.setScaleX(0.5);
      imageSettings.setScaleY(0.5);
      imageUpdate = new ImageView(new Image(getClass().getResourceAsStream("/images/refresh.png")));
      imageUpdate.setScaleX(0.5);
      imageUpdate.setScaleY(0.5);
      imageAdd = new ImageView(new Image(getClass().getResourceAsStream("/images/add.png")));
      imageAdd.setScaleX(0.5);
      imageAdd.setScaleY(0.5);
      imageRename = new ImageView(new Image(getClass().getResourceAsStream("/images/rename.png")));
      imageRename.setScaleX(0.5);
      imageRename.setScaleY(0.5);
      imageDelete = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
      imageDelete.setScaleX(0.5);
      imageDelete.setScaleY(0.5);
      
      stateButton.setGraphic(imagePlay);
      settingsButton.setGraphic(imageSettings);
      refreshButton.setGraphic(imageUpdate);
      refreshButton.setTooltip(new Tooltip(res.getString("tooltip_sync")));
      
      addButton.setGraphic(imageAdd);
      addButton.setOnAction((evt)->createOfflinePlaylist());
      addButton.setTooltip(new Tooltip(res.getString("tooltip_add_offline")));
      renameButton.setGraphic(imageRename);
      renameButton.setOnAction(evt->renamePlaylist());
      renameButton.setTooltip(new Tooltip(res.getString("rename_playlist")));
      deleteButton.setGraphic(imageDelete);
      deleteButton.setOnAction(evt->deletePlaylist());
      deleteButton.setTooltip(new Tooltip(res.getString("delete_playlist")));
      
      loadProgressBar.setCursor(Cursor.HAND);
      volumeSlider.setCursor(Cursor.HAND);
      updatePlaylists();
      if (Settings.lastPlaylistId != null) {
         currentPlaylist = ((ObservableList<Playlist>) playlistsView.getItems()).stream()
            .filter((playlist) -> playlist.getId().equals(Settings.lastPlaylistId))
            .findFirst()
            .orElse(null);
         playlistsView.getSelectionModel().select(currentPlaylist);
         playlistsView.scrollTo(currentPlaylist);
         Platform.runLater(()->{
            if (Settings.lastTrackId != null) {
               currentTrack = ((ObservableList<Track>)tracksView.getItems()).stream()
                  .filter((track) -> track.getId().equals(Settings.lastTrackId))
                  .findFirst()
                  .orElse(null);
               tracksView.getSelectionModel().select(currentTrack);
               tracksView.scrollTo(currentTrack);
            }
         });
      }
      new Timer().scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run() {
            updatePlayProgress();
	 }
      }, 100, 100);
      setupTracksContextMenu();
      
      setupShortcuts();
      
      Platform.runLater(()->{
	 tracksView.requestFocus();
      });
   }
   
   void findPlayingTrack() {
      if(currentPlaylist == playlistsView.getSelectionModel().getSelectedItem()) {
         tracksView.getSelectionModel().select(currentTrack);
         tracksView.scrollTo(currentTrack);
         tracksView.requestFocus();
      } else {
         findTrack = true;
         Platform.runLater(() -> {
            playlistsView.getSelectionModel().select(currentPlaylist);
            playlistsView.scrollTo(currentPlaylist);
         });
      }
   }
   
   void increaseVolume() {
      if (player == null) return;
      double newVolume = Settings.currentVolume + 0.05;
      
      if (newVolume > 1.0) newVolume = 1.0;
      Settings.currentVolume = newVolume;
      player.setVolume(newVolume);
      volumeSlider.setValue(newVolume);
   }
   
   void decreaseVolume() {
      if (player == null) return;
      double newVolume = Settings.currentVolume - 0.05;
      
      if (newVolume < 0.0) newVolume = 0.0;
      Settings.currentVolume = newVolume;
      player.setVolume(newVolume);
      volumeSlider.setValue(newVolume);
   }
   
   public static void afterSettings() {
      setupGlobalHotkeys();
   }
   
   public static void setupGlobalHotkeys() {
      AppController.instance.setupGlobalKeys();
   }
   
   void setupGlobalKeys() {
      hotkeysProvider.reset();
      if(StringUtils.isNotBlank(Settings.Hotkeys.playPause))
	 hotkeysProvider.register(KeyStroke.getKeyStroke(Settings.Hotkeys.playPause), (HotKey hotkey) -> {
	    Platform.runLater(() -> {
	       stateButton.fire();
	    });
	 });
      if(StringUtils.isNotBlank(Settings.Hotkeys.playRandom))
	 hotkeysProvider.register(KeyStroke.getKeyStroke(Settings.Hotkeys.playRandom), (HotKey hotkey) -> {
	    Platform.runLater(() -> {
	       playRandom();
	    });
	 });
      if(StringUtils.isNotBlank(Settings.Hotkeys.playPrev))
	 hotkeysProvider.register(KeyStroke.getKeyStroke(Settings.Hotkeys.playPrev), (HotKey hotkey) -> {
	    Platform.runLater(() -> {
	       if(!interfaceDisabled)
		  playPrevious();
	    });
	 });
      if(StringUtils.isNotBlank(Settings.Hotkeys.deleteAndPlayNext))
	 hotkeysProvider.register(KeyStroke.getKeyStroke(Settings.Hotkeys.deleteAndPlayNext), (HotKey hotkey) -> {
	    Platform.runLater(()-> {
	       deleteTrackFromPlaylist();
	       playNext();
	    });
	 });
      if(StringUtils.isNotBlank(Settings.Hotkeys.addToRemembered))
	 hotkeysProvider.register(KeyStroke.getKeyStroke(Settings.Hotkeys.addToRemembered), (HotKey hotkey) -> {
	    Platform.runLater(() -> {
	       addToRememberedPlaylist(currentTrack);
	    });
	 });
      if(StringUtils.isNotBlank(Settings.Hotkeys.incVolume))
	 hotkeysProvider.register(KeyStroke.getKeyStroke(Settings.Hotkeys.incVolume), (HotKey hotkey) -> {
	    Platform.runLater(() -> {
	       increaseVolume();
	    });
	 });
      if(StringUtils.isNotBlank(Settings.Hotkeys.decVolume))
	 hotkeysProvider.register(KeyStroke.getKeyStroke(Settings.Hotkeys.decVolume), (HotKey hotkey) -> {
	    Platform.runLater(() -> {
	       decreaseVolume();
	    });
	 });
   }
   
   void setupShortcuts() {
      Platform.runLater(() -> {
         stateButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F3), (Runnable) () -> {
	    if(!interfaceDisabled)
	       SwingAppenderUI.getInstance().show();
         });
         stateButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F), (Runnable) () -> {
            if(!interfaceDisabled)
	       findPlayingTrack();
         });
         stateButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F5), (Runnable) () -> {
            if(!interfaceDisabled)
	       Platform.runLater(()->refreshPlaylists());
         });
         stateButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F4), (Runnable) () -> {
            if(numpadOff) {
               setupGlobalKeys();
               Platform.runLater(()->{
                  infoLabel.setText(res.getString("global_hotkeys_on"));
                  log.info("Global hotkeys ON");
               });
            } else {
               hotkeysProvider.reset();
               Platform.runLater(()->{
                  infoLabel.setText(res.getString("global_hotkeys_off"));
                  log.info("Global hotkeys OFF");
               });
            }
            numpadOff = !numpadOff;
         });
         tracksView.setOnKeyPressed((evt) -> {
	    if(interfaceDisabled) return;
            if (evt.getCode().equals(KeyCode.DELETE)) {
               deleteTrackFromPlaylist();
               evt.consume();
            }
            else if (evt.getCode().equals(KeyCode.ENTER)) {
               playSelectedTrack();
               evt.consume();
            }
	    else if (evt.getCode().equals(KeyCode.F2)) {
	       renameTrack();
	       evt.consume();
	    }
         });
	 playlistsView.setOnKeyPressed((evt) -> {
	    if (interfaceDisabled) return;
	    if (evt.getCode().equals(KeyCode.F2)) {
	       renamePlaylist();
	       evt.consume();
	    } 
	 });
      });
      setupGlobalKeys();
   }
   
   void setupPlaylistsContextMenu() {
      ContextMenu cm = playlistsContextMenu;
      MenuItem createOffline = menuItem(res.getString("create_offline_playlist"), ()->{
         createOfflinePlaylist();
      });
      MenuItem setAsFeatured = menuItem(res.getString("set_as_featured"), () -> {
	 Playlist selectedPlaylist = (Playlist) playlistsView.getSelectionModel().getSelectedItem();
	 if(selectedPlaylist != null) {
	    rememberedPlaylist = selectedPlaylist;
	    Settings.rememberedPlaylistId = selectedPlaylist.getId();
	    infoLabel.setText(String.format(res.getString("featured_set"), selectedPlaylist.getTitle()));
	 }
      });
      MenuItem rename = menuItem("(...) "+res.getString("rename_playlist"), ()->{
         renamePlaylist();
      });
      MenuItem delete = menuItem("(-) "+res.getString("delete_playlist"), ()->{
         deletePlaylist();
      });
      cm.getItems().addAll(
	      createOffline,
	      new SeparatorMenuItem(),
	      setAsFeatured, 
	      new SeparatorMenuItem(),
	      rename, 
	      delete);
   }
   
   void setupTracksContextMenu() {
      ContextMenu cm = tracksContextMenu;
      List<MenuItem> items = cm.getItems();
      items.clear();
      
      Playlist pl = getSelectedPlaylist();
      if(pl == null) {
         items.add(menuItem("-> " + res.getString("add_to"), () -> {
            menuAddToPlaylist();
         }));
	 if (rememberedPlaylist != null) {
	    items.add(menuItem("-> " + String.format(res.getString("add_to_featured"), rememberedPlaylist.getTitle()), () -> {
	       Track t = getSelectedTrack();
	       if (t != null) {
		  addToPlaylist(t, rememberedPlaylist);
	       }
	    }));
	 }
      } else {
	 items.add(menuItem("(+) " + res.getString("add_files"), () -> {
	    FileChooser fileChooser = new FileChooser();
	    fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
	    fileChooser.setTitle(String.format(res.getString("add_files_to"), pl.getTitle()));
	    fileChooser.getExtensionFilters().addAll(
	       new FileChooser.ExtensionFilter("MP3", "*.mp3")
	    );
	    List<File> files = fileChooser.showOpenMultipleDialog(null);
	    if (files != null) {
	       addToPlaylist(files, pl);
	    }
	 }));
	 items.add(menuItem("(+) " + res.getString("add_folder_recursively"), () -> {
	    DirectoryChooser dirChooser = new DirectoryChooser();
	    dirChooser.setTitle(String.format(res.getString("add_folder_to"), pl.getTitle()));
	    dirChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
	    File dir = dirChooser.showDialog(null);
	    if(dir == null) return;
	    Collection<File> files = FileUtils.listFiles(dir, new String[] { "mp3" }, true);
	    addToPlaylist(new ArrayList(files), pl);
	 }));
	 items.add(new SeparatorMenuItem());
	 if(rememberedPlaylist != null)
	    items.add(menuItem("-> " + String.format(res.getString("add_to_featured"), rememberedPlaylist.getTitle()), () -> {
	       Track t = getSelectedTrack();
	       if(t != null)
		  addToPlaylist(t, rememberedPlaylist);
	    }));
	 items.add(menuItem("-> " + res.getString("add_to"), ()->{
	    menuAddToPlaylist();
	 }));
	 items.add(new SeparatorMenuItem());
	 items.add(menuItem("(...) " + res.getString("rename_track"), ()->{
	    renameTrack();
	 }));
	 items.add(new SeparatorMenuItem());
	 items.add(menuItem("(-) " + res.getString("delete_track") + pl.getTitle(), () -> {
	    deleteTrackFromPlaylist();
	 }));
	 items.add(menuItem("(-) " + res.getString("delete_dead_items"), () -> {
	    Track t = getSelectedTrack();
	    if (t == null) {
	       return;
	    }
	    deleteDeadFromOfflinePlaylist(pl);
	 }));
      }
   }
   
   void addToPlaylist(List<File> files, Playlist p) {
      if(interfaceDisabled) return;
      new Thread(()->{
	 setInterfaceDisabled(true);
	 int total = files.size();
	 AtomicInteger current = new AtomicInteger(0);
	 Cache.pushToPlaylist(files, p, (Track t) -> {
	    Platform.runLater(()->{
	       infoLabel.setText(String.format(res.getString("processed"), current.incrementAndGet(), total, t.getTitle()));
	       if(p == getSelectedPlaylist()) {
		  tracksView.getItems().remove(t);
		  tracksView.getItems().add(0, t);
	       }
	    });
	 });
	 setInterfaceDisabled(false);
      }).start();
      if(p == getSelectedPlaylist()) {
         Platform.runLater(()->{
            loadSelectedPlaylist();
         });
      }
   }
   
   private void setInterfaceDisabled(boolean disabled) {
      playlistsView.setDisable(disabled);
      searchButton.setDisable(disabled);
      searchText.setDisable(disabled);
      addButton.setDisable(disabled);
      renameButton.setDisable(disabled);
      deleteButton.setDisable(disabled);
      refreshButton.setDisable(disabled);
      interfaceDisabled = disabled;
   }
   
   void addToPlaylist(Track t, Playlist p) {
      if(interfaceDisabled) return;
      Cache.pushToPlaylist(t, p);
      if (p == getSelectedPlaylist()) {
	 Platform.runLater(() -> {
	    loadSelectedPlaylist();
	 });
      }
   }
   
   void deleteFromOfflinePlaylist(Track t, Playlist p) {
      if(interfaceDisabled) return;
      tracksView.getItems().remove(t);
      Cache.deleteFromPlaylist(t, p);
   }
   
   void deleteDeadFromOfflinePlaylist(Playlist p) {
      if(interfaceDisabled) return;
      infoLabel.setText(res.getString("deleting_dead_items"));
      log.info("Deleting dead items from " + p.getTitle());
      Integer deleted = Cache.deleteDead(p);
      infoLabel.setText(String.format(res.getString("deleted_dead_items"), deleted));
      log.info("Deleted dead items from " + p.getTitle() + ": " + deleted);
      Platform.runLater(()->{
         loadSelectedPlaylist();
      });
   }
   
   private MenuItem menuItem(String title, Runnable r) {
      MenuItem mi = new MenuItem(title);
      mi.setOnAction(event -> {
         r.run();
      });
      return mi;
   }
   
   void createOfflinePlaylist() {
      if(interfaceDisabled) return;
      TextInputDialog dialog = new TextInputDialog(res.getString("new_offline_playlist"));
      dialog.setTitle(res.getString("create_new_offline_playlist"));
      dialog.setHeaderText(res.getString("create_new_playlist"));
      dialog.setContentText(res.getString("enter_playlist_name"));
      dialog.getDialogPane().getStylesheets().add("/styles/dialogs.css");
      ((Stage)dialog.getDialogPane().getScene().getWindow()).getIcons().addAll(logoImages);
      Optional<String> result = dialog.showAndWait();
      result.ifPresent(title -> {
         Platform.runLater(()->{
            Playlist newPlaylist = Cache.createPlaylist(title);
            Platform.runLater(()->{
               playlistsView.getItems().add(newPlaylist);
               playlistsView.getSelectionModel().select(newPlaylist);
            });
         });
      });
   }
   
   void renamePlaylist() {
      if(interfaceDisabled) return;
      Playlist p = getSelectedPlaylist();
      if(p == null) return;

      TextInputDialog dialog = new TextInputDialog(p.getTitle());
      dialog.setTitle(res.getString("rename_playlist"));
      dialog.setHeaderText(res.getString("rename_playlist"));
      dialog.setContentText(res.getString("enter_new_title"));
      dialog.getDialogPane().getStylesheets().add("/styles/dialogs.css");
      ((Stage)dialog.getDialogPane().getScene().getWindow()).getIcons().addAll(logoImages);
      Optional<String> result = dialog.showAndWait();
      result.ifPresent(title -> {
         if (StringUtils.isEmpty(title)) {
            return;
         }
	 Cache.renamePlaylist(p, title);
	 Platform.runLater(()->{
	    updatePlaylists();
	 });
      });
   }
   
   void renameTrack() {
      if(interfaceDisabled) return;
      Playlist p = getSelectedPlaylist();
      if(p == null) return;
      Track t = getSelectedTrack();
      if(t == null) return;
      
      TextInputDialog dialog = new TextInputDialog(t.getTitle());
      dialog.setTitle(res.getString("rename_track"));
      dialog.setHeaderText(res.getString("rename_track"));
      dialog.setContentText(res.getString("enter_new_title"));
      dialog.getDialogPane().getStylesheets().add("/styles/dialogs.css");
      ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().addAll(logoImages);
      Optional<String> result = dialog.showAndWait();
      result.ifPresent(title -> {
	 if (StringUtils.isEmpty(title)) {
	    return;
	 }
	 Cache.renameTrack(t, p, title);
	 Platform.runLater(() -> {
	    loadSelectedPlaylist();
	 });
      });
   }
   
   void deletePlaylist() {
      if(interfaceDisabled) return;
      Playlist p = getSelectedPlaylist();
      if(p == null) return;
      Alert alert = new Alert(AlertType.CONFIRMATION);
      ButtonType btYes = new ButtonType(res.getString("yes"), ButtonBar.ButtonData.YES);
      ButtonType btCancel = new ButtonType(res.getString("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
      alert.getButtonTypes().setAll(btYes, btCancel);
      alert.setTitle(res.getString("delete_playlist"));
      alert.setHeaderText(res.getString("delete_playlist"));
      alert.setContentText(String.format(res.getString("delete_confirm"), p.getTitle()));
      alert.getDialogPane().getStylesheets().add("/styles/dialogs.css");
      ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().addAll(logoImages);

      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == btYes) {
	 Cache.deletePlaylist(p);
	 Platform.runLater(() -> {
	    updatePlaylists();
	 });
      }
   }
   
   Playlist getSelectedPlaylist() {
      if(searching) return null;
      return (Playlist) playlistsView.getSelectionModel().getSelectedItem();
   }
   
   Track getSelectedTrack() {
      return (Track) tracksView.getSelectionModel().getSelectedItem();
   }
   
   @FXML
   void showSettings() {
      SettingsWindow.show();
   }
   
   @FXML
   public void tracksViewMouseClicked(MouseEvent evt) {
      if(evt.getClickCount() == 2) {
         playSelectedTrack();
      }
   }
   
   void playSelectedTrack() {
      Track t = (Track) tracksView.getSelectionModel().getSelectedItem();
      if(t != null)
         Settings.lastTrackId = t.getId();
      playTrack(t);
   }
   
   void deleteTrackFromPlaylist() {
      Track t = getSelectedTrack();
      if (t == null) {
         return;
      }
      Playlist pl = getSelectedPlaylist();
      if(pl != null) {
         deleteFromOfflinePlaylist(t, pl);
      }
   }
   
   public static final Image[] logoImages = new Image[] { 
      new Image(AppController.class.getResourceAsStream("/images/logo_tray_16.png")),
      new Image(AppController.class.getResourceAsStream("/images/logo_tray_32.png")),
      new Image(AppController.class.getResourceAsStream("/images/logo_tray_48.png")),
      new Image(AppController.class.getResourceAsStream("/images/logo_tray_64.png")),
      new Image(AppController.class.getResourceAsStream("/images/logo_tray_96.png")),
      new Image(AppController.class.getResourceAsStream("/images/logo_tray_128.png"))
   };
   
   private ChoiceDialog<Playlist> playlistSelectionDialog(List<Playlist> items) {
      ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(null, items);
      dialog.setTitle(res.getString("add_to"));
      dialog.setGraphic(null);
      ((Stage)dialog.getDialogPane().getScene().getWindow()).getIcons().addAll(logoImages);
      dialog.setHeaderText(res.getString("choose_playlist"));
      dialog.getDialogPane().getStylesheets().add("/styles/dialogs.css");
      if(rememberedPlaylist != null)
	 dialog.setSelectedItem(rememberedPlaylist);
      return dialog;
   }
   
   void menuAddToPlaylist() {
      Track t = getSelectedTrack();
      if (t == null) {
         return;
      }
      ChoiceDialog<Playlist> dialog = playlistSelectionDialog(playlistsView.getItems());
      Optional<Playlist> result = dialog.showAndWait();
      result.ifPresent(playlist -> addToPlaylist(t, playlist));
   }
   
   void addToRememberedPlaylist(Track t) {
      if(interfaceDisabled) return;
      if(t == null) return;
      if(rememberedPlaylist != null) {
	 Cache.pushToPlaylist(t, rememberedPlaylist);
	 infoAndAnnounce(String.format(res.getString("track_added_to_playlist"),  rememberedPlaylist.getTitle(), t.getTitle()));
	 if(rememberedPlaylist == getSelectedPlaylist())
	    loadSelectedPlaylist();
      } else {
	 infoAndAnnounce(res.getString("remembered_not_yet"));
      }
   }
   
   private void infoAndAnnounce(String message) {
      infoLabel.setText(message);
      Tray.announce(message);
   }
   
   String formatTime(Duration total, Duration current) {
      return String.format("%01d:%02d / %01d:%02d", 
         (int)current.toMinutes(), (int)current.toSeconds() % 60,
         (int)total.toMinutes(), (int)total.toSeconds() % 60);
   }

   public void setStage(Stage stage) {
      this.stage = stage;
   }
   
   private static class PlaylistAndTrack {
      private Playlist playlist;
      private Track track;
      
      PlaylistAndTrack(Playlist playlist, Track track) {
	 this.playlist = playlist;
	 this.track = track;
      }

      public void setPlaylist(Playlist playlist) {
	 this.playlist = playlist;
      }

      public void setTrack(Track track) {
	 this.track = track;
      }
   }
}
