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

import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 *
 * @author OlegusGetman
 */
public class Settings {
   private static final ResourceBundle res = App.res;
   
   public static final String MODE_NEXT = res.getString("next");
   public static final String MODE_RANDOM = res.getString("random");
   public static final String MODE_SAME = res.getString("same");
  
   public static String currentMode = MODE_NEXT;
   public static Double currentVolume = 0.5;
   public static String storagePath = Paths.get("").toAbsolutePath().toString() + "/weeplayer_storage";
   public static String email = "";
   public static String password = "";
   public static String lastPlaylistId = null;
   public static String lastTrackId = null;
   public static String rememberedPlaylistId = null;
   public static boolean trayWhenMinimized = false;
   public static boolean trayMessages = true;
   public static double width = 940;
   public static double height = 580;
   public static boolean maximized = false;
   
   private static final Preferences prefs = Preferences.userRoot();
   
   public static void save() {
      prefs.put("weeplayer_mode", currentMode);
      prefs.putDouble("weeplayer_volume", currentVolume);
      prefs.put("weeplayer_storage", storagePath);
      prefs.put("weeplayer_email", email);
      prefs.put("weeplayer_password", password);
      prefs.putBoolean("weeplayer_tray", trayWhenMinimized);
      prefs.putDouble("weeplayer_width", width);
      prefs.putDouble("weeplayer_height", height);
      prefs.putBoolean("weeplayer_maximized", maximized);
      prefs.putBoolean("weeplayer_trayMessages", trayMessages);
      saveHotkeys();
      
      if(lastPlaylistId != null)
         prefs.put("weeplayer_lastPlaylistId", lastPlaylistId);
      if(lastTrackId != null)
         prefs.put("weeplayer_lastTrackId", lastTrackId);
      if(rememberedPlaylistId != null)
	 prefs.put("weeplayer_rememberedPlaylistId", rememberedPlaylistId);
   }
   
   private static void saveHotkeys() {
      prefs.put("weeplayer_hotkey_playPause", Hotkeys.playPause);
      prefs.put("weeplayer_hotkey_playRandom", Hotkeys.playRandom);
      prefs.put("weeplayer_hotkey_playPrev", Hotkeys.playPrev);
      prefs.put("weeplayer_hotkey_deleteAndPlayNext", Hotkeys.deleteAndPlayNext);
      prefs.put("weeplayer_hotkey_addToRemembered", Hotkeys.addToRemembered);
      prefs.put("weeplayer_hotkey_incVolume", Hotkeys.incVolume);
      prefs.put("weeplayer_hotkey_decVolume", Hotkeys.decVolume);
   }
   
   public static void load() {
      currentMode = prefs.get("weeplayer_mode", MODE_NEXT);
      currentVolume = prefs.getDouble("weeplayer_volume", 0.5);
      storagePath = prefs.get("weeplayer_storage", storagePath);
      email = prefs.get("weeplayer_email", email);
      password = prefs.get("weeplayer_password", password);
      trayWhenMinimized = prefs.getBoolean("weeplayer_tray", trayWhenMinimized);
      width = prefs.getDouble("weeplayer_width", width);
      height = prefs.getDouble("weeplayer_height", height);
      maximized = prefs.getBoolean("weeplayer_maximized", maximized);
      trayMessages = prefs.getBoolean("weeplayer_trayMessages", trayMessages);
      lastPlaylistId = prefs.get("weeplayer_lastPlaylistId", lastPlaylistId);
      lastTrackId = prefs.get("weeplayer_lastTrackId", lastTrackId);
      rememberedPlaylistId = prefs.get("weeplayer_rememberedPlaylistId", rememberedPlaylistId);
      loadHotkeys();
   }
   
   private static void loadHotkeys() {
      Hotkeys.playPause = prefs.get("weeplayer_hotkey_playPause", Hotkeys.playPause);
      Hotkeys.playRandom = prefs.get("weeplayer_hotkey_playRandom", Hotkeys.playRandom);
      Hotkeys.playPrev = prefs.get("weeplayer_hotkey_playPrev", Hotkeys.playPrev);
      Hotkeys.deleteAndPlayNext = prefs.get("weeplayer_hotkey_deleteAndPlayNext", Hotkeys.deleteAndPlayNext);
      Hotkeys.addToRemembered = prefs.get("weeplayer_hotkey_addToRemembered", Hotkeys.addToRemembered);
      Hotkeys.incVolume = prefs.get("weeplayer_hotkey_incVolume", Hotkeys.incVolume);
      Hotkeys.decVolume = prefs.get("weeplayer_hotkey_decVolume", Hotkeys.decVolume);
   }
   
   public static class Hotkeys {
      public static String playPause = "alt P";
      public static String playRandom = "control shift RIGHT";
      public static String playPrev = "control shift LEFT";
      public static String deleteAndPlayNext = "control shift DELETE";
      public static String addToRemembered = "control shift INSERT";
      public static String incVolume = "NUMPAD8";
      public static String decVolume = "NUMPAD2";
   }
}
