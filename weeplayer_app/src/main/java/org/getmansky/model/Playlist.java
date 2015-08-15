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
package org.getmansky.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author OlegusGetman
 */
public class Playlist implements Serializable {
   private String id;
   private String title;
   private List<Track> tracks;
   private Boolean offline = false;

   public Integer getCount() {
      return tracks.size();
   }
   
   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public List<Track> getTracks() {
      return tracks;
   }

   public void setTracks(List<Track> tracks) {
      this.tracks = tracks;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public Boolean getOffline() {
      return offline;
   }

   public void setOffline(Boolean offline) {
      this.offline = offline;
   }
   
   @Override
   public String toString() {
      return title;
   }

   @Override
   public int hashCode() {
      int hash = 5;
      hash = 23 * hash + Objects.hashCode(this.id);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final Playlist other = (Playlist) obj;
      if (!Objects.equals(this.id, other.id)) {
         return false;
      }
      return true;
   }
   
   
}
