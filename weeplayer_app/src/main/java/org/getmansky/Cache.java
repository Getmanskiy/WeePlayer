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

import org.getmansky.util.EventHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.getmansky.model.Playlist;
import org.getmansky.model.Track;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.tritonus.share.sampled.file.TAudioFileFormat;

/**
 *
 * @author OlegusGetman
 */
public class Cache {
   
   private final static String PLAYLISTS_PATH = "/playlists/";
   private final static String TRACKS_PATH = "/tracks/";
   private final static Logger log = Logger.getLogger(Cache.class);
   
   /*static {
      List<Playlist> playlists = playlists();
      final AtomicInteger cur = new AtomicInteger(0);
      final AtomicInteger cnt = new AtomicInteger(0);
      playlists.forEach(p -> { cnt.addAndGet(p.getTracks().size()); });
      playlists.forEach(playlist -> playlist.getTracks().forEach(track -> {
	 try {
	    AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(getContent(track));
	    Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
	    String title = (String) properties.get("title");
	    String author = (String) properties.get("author");
	    if(StringUtils.isNotBlank(title) && StringUtils.isNotBlank(author)) {
	       track.setTitle(author + " — " + title);
	       log.info("["+cur.incrementAndGet() + " / " + cnt.get() + "] " + author + " — " + title);
	    }
	 } catch (UnsupportedAudioFileException | IOException ex) {
	    log.fatal(ex, ex);
	 }
      }));
      savePlaylists(playlists);
   }*/
   
   private static String getNameFromTags(File file) {
      try {
	 AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
	 Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
	 String title = (String) properties.get("title");
	 String author = (String) properties.get("author");
	 if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(author)) {
	    return author + " — " + title;
	 }
      } catch (UnsupportedAudioFileException | IOException ex) {
	 log.fatal(ex, ex);
      }
      return null;
   }
   
   private static double getDuration(File file) throws UnsupportedAudioFileException, IOException {
      AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
      if (fileFormat instanceof TAudioFileFormat) {
	 Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
	 String key = "duration";
	 Long microseconds = (Long) properties.get(key);
	 return microseconds / 1000;
      } else {
	 throw new UnsupportedAudioFileException();
      }
   }
   
   private static void savePlaylists(List<Playlist> playlists) {
      BufferedOutputStream buf = null;
      try {
         File cacheDir = new File(Settings.storagePath + PLAYLISTS_PATH);
         if (!cacheDir.exists()) {
            cacheDir.mkdirs();
         }
         
         File cacheFasterFile = new File(Settings.storagePath + PLAYLISTS_PATH + "list");
         cacheFasterFile.delete();
         buf = new BufferedOutputStream(new FileOutputStream(cacheFasterFile));
         try (FSTObjectOutput output = new FSTObjectOutput(buf)) {
            output.writeObject(playlists);
            output.flush();
         }
         
      } catch (FileNotFoundException ex) {
         log.log(Level.ERROR, null, ex);
      } catch (IOException ex) {
	 log.log(Level.ERROR, null, ex);
      } finally {
         try {
            if (buf != null) {
               buf.close();
            }
         } catch (IOException ex) {
            log.log(Level.ERROR, null, ex);
         }
      }
   }
   
   public static List<Playlist> cachedPlaylists;
   public static List<Playlist> playlists() {
      File cachedFile = new File(Settings.storagePath + PLAYLISTS_PATH + "list");
      if (cachedFile.exists()) {
         try {
            List<Playlist> playlists = null;
            try (FSTObjectInput input = new FSTObjectInput(new BufferedInputStream(new FileInputStream(cachedFile)))) {
               playlists = (List<Playlist>) input.readObject();
               playlists = playlists.stream().sorted((pl1, pl2) -> {
                  return pl1.getTitle().compareTo(pl2.getTitle());
               }).collect(Collectors.toList());
               
            } catch (IOException ex) {
               log.log(Level.ERROR, null, ex);
               return null;
            }            
            return cachedPlaylists = playlists;
         } catch (ClassNotFoundException ex) {
            return cachedPlaylists = new ArrayList<>();
         }
      } else {
         return cachedPlaylists = new ArrayList<>();
      }
   }
   
   public static File getContent(Track t) {
      return new File(Settings.storagePath + "/" + TRACKS_PATH + "/" + t.getId());
   }
   
   public static Playlist createPlaylist(String title) {
      Playlist p = new Playlist();
      p.setTitle(title);
      p.setId("offline-"+new Date());
      p.setOffline(Boolean.TRUE);
      p.setTracks(new ArrayList<>());
      
      List<Playlist> pls = new ArrayList(playlists());
      pls.add(p);
      savePlaylists(pls);
      
      return p;
   }
   
   public static Playlist renamePlaylist(Playlist p, String title) {
      p.setTitle(title);
      List<Playlist> pls = new ArrayList(playlists());
      pls.remove(p);
      pls.add(p);
      savePlaylists(pls);
      return p;
   }
   
   public static void deletePlaylist(Playlist p) {
      List<Playlist> pls = new ArrayList(playlists());
      pls.remove(p);
      savePlaylists(pls);
   }
   
   public static Playlist pushToPlaylist(List<File> files, Playlist p, EventHandler<Track> handler) {
      List<Playlist> pls = new ArrayList(playlists());
      
      File fullTracksPath = new File(Settings.storagePath + "/" + TRACKS_PATH);
      fullTracksPath.mkdirs();
      
      files.stream().forEach((file) -> {
	 Track t = new Track();
	 t.setOffline(Boolean.TRUE);
	 try {
	    String md5 = DigestUtils.md5Hex(new FileInputStream(file));
	    String newFilename = md5 + "-" + file.length() + ".mp3";
	    Files.copy(file.toPath(), Paths.get(Settings.storagePath, TRACKS_PATH, newFilename), StandardCopyOption.REPLACE_EXISTING);
	    t.setId(newFilename);
	    String nameFromTags = getNameFromTags(file);
	    if(nameFromTags != null) {
	       t.setTitle(nameFromTags);
	    } else {
	       t.setTitle(file.getName().substring(0, file.getName().length() - 4)); //remove ".mp3" at the end
	    }
	    t.setDuration(getDuration(file));
	    p.getTracks().remove(t);
	    p.getTracks().add(t);
	    handler.handle(t);
	 } catch (IOException | UnsupportedAudioFileException ex) {
	    log.fatal(ex, ex);
	 }
      });
      pls.remove(p);
      pls.add(p);
      
      savePlaylists(pls);
      return p;
   }
   
   public static Playlist pushToPlaylist(Track t, Playlist p) {
      List<Playlist> pls = new ArrayList(playlists());
      
      p.getTracks().remove(t);
      p.getTracks().add(t);
      pls.remove(p);
      pls.add(p);
      
      savePlaylists(pls);
      return p;
   }
   
   public static void renameTrack(Track t, Playlist p, String title) {
      for(Track track : p.getTracks()) {
	 if(track == t) {
	    t.setTitle(title);
	    break;
	 }
      }
      
      List<Playlist> playlists = playlists();
      playlists.remove(p);
      playlists.add(p);
      savePlaylists(playlists);
   }
   
   public static void deleteFromPlaylist(Track t, Playlist p) {
      p.getTracks().remove(t);
      
      List<Playlist> pls = new ArrayList(playlists());
      pls.remove(p);
      pls.add(p);
      savePlaylists(pls);
   }
   
   public static Integer deleteDead(Playlist p) {
      Integer cnt = 0;
      List<Track> tracks = p.getTracks();
      for(Track t : tracks) {
         if(t.getId() != null) {
            File f = new File(Settings.storagePath + "/" + TRACKS_PATH + "/" + t.getId());
            if (!f.exists()) {
               t.setId(null);
               cnt++;
            }
         }
      }
      
      for(int i = 0; i < tracks.size(); i++) {
         if(tracks.get(i).getId() == null) {
            tracks.remove(i--);
         }
      }
      
      List<Playlist> pls = new ArrayList(playlists());
      pls.remove(p);
      pls.add(p);
      savePlaylists(pls);
      return cnt;
   }
   
   public static List<Track> search(String text) {
      List<Track> totalTracks = new ArrayList();
      cachedPlaylists
	 .stream()
         .map(playlist -> playlist.getTracks())
         .forEach(tracks ->{
            totalTracks.addAll(tracks);
         });
      return totalTracks
         .stream()
         .distinct()
         .filter(track -> track.getTitle().toLowerCase().contains(text.toLowerCase()))
         .collect(Collectors.toList());
   }
}
