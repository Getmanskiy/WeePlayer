package org.getmansky;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class App extends Application {
   
   private static Stage stage;
   private static final Logger log = Logger.getLogger(App.class);
   public static Locale locale = Locale.getDefault();
   public final static ResourceBundle res = ResourceBundle.getBundle("locales.locale", locale);

   public static void main(String[] args) throws Exception {
      try {
         launch(args);
      } catch(Throwable e) {
         log.log(Level.ERROR, null, e);
      }
   }

   @Override
   public void start(Stage stage) throws Exception {
      App.stage = stage;
      
      Settings.load();
      
      FXMLLoader loader = new FXMLLoader();
      loader.setResources(res);
      Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream("/fxml/app.fxml"));
      
      Scene scene = new Scene(rootNode, Settings.width, Settings.height);
      
      stage.getIcons().addAll(AppController.logoImages);

      //scene.getStylesheets().add("http://fonts.googleapis.com/css?family=PT+Sans+Caption&subset=latin,cyrillic");
      
      scene.getStylesheets().add("/styles/styles.css");
      stage.setTitle("WeePlayer");
      stage.setScene(scene);
      stage.setMinWidth(740);
      stage.setMinHeight(480);
      
      stage.setWidth(Settings.width);
      stage.setHeight(Settings.height);
      stage.setMaximized(Settings.maximized);
      
      ((AppController) loader.getController()).setStage(stage);
      
      Platform.setImplicitExit(false);
 
      Tray.setup(stage, res);
      AppController.afterSettings();
      stage.setOnShown((evt)->{
	 ((AppController) loader.getController()).onShown();
      });
      stage.show();
      stage.setOnCloseRequest((WindowEvent evt) -> {
         if(!exit()) evt.consume();
      });
      stage.setOnHiding((WindowEvent evt) -> {
         Settings.width = stage.getWidth();
         Settings.height = stage.getHeight();
         Settings.maximized = stage.isMaximized();
      });
      stage.iconifiedProperty().addListener((ObservableValue<? extends Boolean> prop, Boolean oldValue, Boolean newValue) -> {
         if(newValue && Settings.trayWhenMinimized) {
            Settings.width = stage.getWidth();
            Settings.height = stage.getHeight();
            Settings.maximized = stage.isMaximized();
            stage.hide();
         }
      });
      log.log(Level.INFO, "app launched");
   }
   
   public static boolean exit() {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      ButtonType btYes = new ButtonType(res.getString("yes"), ButtonData.YES);
      ButtonType btNo = new ButtonType(res.getString("no"), ButtonData.NO);
      alert.getButtonTypes().setAll(btYes, btNo);
      alert.setTitle("WeePlayer");
      alert.setHeaderText(null);
      alert.setContentText(res.getString("exit_confirm"));
      alert.getDialogPane().getStylesheets().add("/styles/dialogs.css");
      ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(AppController.logoImages);

      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == btYes) {
         new Thread(() -> {
            if(stage.isShowing()) {
               Settings.width = stage.getWidth();
               Settings.height = stage.getHeight();
               Settings.maximized = stage.isMaximized();
            }
            Settings.save();
            log.log(Level.INFO, "exiting app");
            System.exit(0);
         }).start();
         return true;
      } else {
         return false;
      }
   }
}
