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

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tray.SystemTrayAdapter;
import tray.SystemTrayProvider;
import tray.TrayIconAdapter;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ResourceBundle;

import static org.getmansky.AppController.logoImages;

/**
 * @author OlegusGetman
 */
public class Tray {
    public static Stage stage = null;

    private static TrayIconAdapter trayIcon;

    public static void announce(String mesage) {
        if (!Settings.trayMessages) return;
        trayIcon.displayMessage("", mesage, TrayIcon.MessageType.INFO);
    }

    public static void setup(Stage stage, ResourceBundle res) {
        Tray.stage = stage;
        SystemTrayProvider trayProv = new SystemTrayProvider();
        SystemTrayAdapter trayAdapter = trayProv.getSystemTray();
        URL imageURL = Tray.class.getResource("/images/logo_tray_16.png");
        String tooltip = "WeePlayer";

        PopupMenu menu = new PopupMenu();
        menu.add(menuItem(res.getString("show_weeplayer"), (event) -> {
            Platform.runLater(() -> {
                showStage();
            });
        }));
        menu.add(menuItem(res.getString("about"), (event) -> {
            Platform.runLater(() -> {
                showAbout();
            });
        }));
        menu.add(menuItem(res.getString("settings"), (event) -> {
            Platform.runLater(() -> {
                SettingsWindow.show();
            });
        }));
        menu.add(menuItem(res.getString("exit"), (event) -> {
            Platform.runLater(() -> {
                App.exit();
            });
        }));

        trayIcon = trayAdapter.createAndAddTrayIcon(imageURL, tooltip, menu);
        trayIcon.setImageAutoSize(false);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Platform.runLater(() -> {
                        showStage();
                    });
                }
            }
        });
    }

    private static void showStage() {
        if (stage != null) {
            stage.setIconified(false);
            stage.toFront();
            stage.setWidth(Settings.width);
            stage.setHeight(Settings.height);
            stage.setMaximized(Settings.maximized);
            stage.show();
        }
    }

    private static void showAbout() {
        Node imageLogo = new ImageView(new Image(Tray.class.getResourceAsStream("/images/logo_tray_96.png")));

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About WeePlayer");
        alert.setHeaderText("WeePlayer"
                + "\nVersion: " + Tray.class.getPackage().getImplementationVersion());
        alert.setGraphic(imageLogo);
        alert.setContentText("Copyright (C) 2015 Oleg Getmanskiy"
                + "\n"
                + "\nThis program is free software: you can redistribute it and/or modify"
                + "\nit under the terms of the GNU General Public License as published by"
                + "\nthe Free Software Foundation, either version 3 of the License, or"
                + "\n(at your option) any later version."
                + "\n"
                + "\nThis program is distributed in the hope that it will be useful,"
                + "\nbut WITHOUT ANY WARRANTY; without even the implied warranty of"
                + "\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
                + "\nGNU General Public License for more details."
                + "\n"
                + "\nYou should have received a copy of the GNU General Public License"
                + "\nalong with this program.  If not, see <http://www.gnu.org/licenses/>."
                + "\n");
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(logoImages);
        alert.getDialogPane().getStylesheets().add("/styles/dialogs.css");
        alert.setResizable(true);
        alert.show();
        alert.setWidth(460);
        alert.setHeight(490);
        alert.setResizable(false);
    }

    private static MenuItem menuItem(String title, ActionListener al) {
        MenuItem mi = new MenuItem(title);
        mi.addActionListener(al);
        return mi;
    }
}
