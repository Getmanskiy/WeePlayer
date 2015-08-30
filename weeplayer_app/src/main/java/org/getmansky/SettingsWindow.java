/*
 * Copyright (C) 2015 Oleg Getmansky aka OlegusGetman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.getmansky;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author OlegusGetman
 */
public class SettingsWindow implements Initializable {

    public static Stage stage;
    private static ResourceBundle res;
    @FXML
    private TextField storagePathText;
    @FXML
    private CheckBox traySetting;
    @FXML
    private CheckBox trayMessages;
    @FXML
    private TextField keyDeleteAndPlayNext;
    @FXML
    private TextField keyAddToRemembered;
    @FXML
    private TextField keyPlayPause;
    @FXML
    private TextField keyPlayRandom;
    @FXML
    private TextField keyPlayPrev;
    @FXML
    private TextField keyDecVolume;
    @FXML
    private TextField keyIncVolume;

    public static void init() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(ResourceBundle.getBundle("locales.locale", App.locale));
            Parent rootNode = (Parent) loader.load(SettingsWindow.class.getResourceAsStream("/fxml/settings.fxml"));
            Scene scene = new Scene(rootNode, 480, 485);
            scene.getStylesheets().add("/styles/styles.css");
            SettingsWindow.stage = new Stage();
            stage.getIcons().addAll(AppController.logoImages);

            SettingsWindow.stage.setResizable(false);
            SettingsWindow.stage.setTitle(res.getString("settings"));
            SettingsWindow.stage.setScene(scene);
            SettingsWindow.stage.show();
        } catch (IOException ex) {
            Logger.getLogger(SettingsWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void show() {
        if (stage == null) init();
        stage.show();
    }

    @FXML
    public void apply() {
        Settings.storagePath = storagePathText.getText();
        stage.close();

        Settings.save();
        AppController.afterSettings();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        res = resources;
        storagePathText.setText(Settings.storagePath);
        traySetting.setSelected(Settings.trayWhenMinimized);
        traySetting.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
            Settings.trayWhenMinimized = newVal;
        });
        trayMessages.setSelected(Settings.trayMessages);
        trayMessages.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
            Settings.trayMessages = newVal;
        });

        initHotkeyConfig();
    }

    private void initHotkeyConfig() {
        keyAddToRemembered.setText(Settings.Hotkeys.addToRemembered);
        keyDeleteAndPlayNext.setText(Settings.Hotkeys.deleteAndPlayNext);
        keyPlayPause.setText(Settings.Hotkeys.playPause);
        keyPlayRandom.setText(Settings.Hotkeys.playRandom);
        keyPlayPrev.setText(Settings.Hotkeys.playPrev);
        keyDecVolume.setText(Settings.Hotkeys.decVolume);
        keyIncVolume.setText(Settings.Hotkeys.incVolume);

        keyDeleteAndPlayNext.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldVal, String newVal) {
                Settings.Hotkeys.deleteAndPlayNext = newVal;
            }
        });
        keyPlayPause.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldVal, String newVal) {
                Settings.Hotkeys.playPause = newVal;
            }
        });
        keyPlayRandom.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldVal, String newVal) {
                Settings.Hotkeys.playRandom = newVal;
            }
        });
        keyPlayPrev.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldVal, String newVal) {
                Settings.Hotkeys.playPrev = newVal;
            }
        });
        keyDecVolume.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldVal, String newVal) {
                Settings.Hotkeys.decVolume = newVal;
            }
        });
        keyIncVolume.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldVal, String newVal) {
                Settings.Hotkeys.incVolume = newVal;
            }
        });
    }

    @FXML
    private void setStoragePath() {
        DirectoryChooser dc = new DirectoryChooser();
        File dir = dc.showDialog(stage);
        if (dir != null)
            storagePathText.setText(dir.getAbsolutePath());
    }
}
