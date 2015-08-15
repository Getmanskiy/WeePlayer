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
import java.util.Objects;

/**
 *
 * @author OlegusGetman
 */
public class Track implements Serializable {
   
   private String id;
   private String title;
   private Boolean offline = false;
   private Double duration = 0d;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public Boolean getOffline() {
      return offline;
   }

   public void setOffline(Boolean offline) {
      this.offline = offline;
   }

   public Double getDuration() {
      return duration;
   }
   
   public String getDurationFormatted() {
      int min = (int) (duration / 1000 / 60);
      int sec = (int) (duration / 1000 % 60);
      return String.format("%01d:%02d", min, sec);
   }

   public void setDuration(Double duration) {
      this.duration = duration;
   }

   @Override
   public String toString() {
      return title;
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 13 * hash + Objects.hashCode(this.id);
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
      final Track other = (Track) obj;
      if (!Objects.equals(this.id, other.id)) {
         return false;
      }
      return true;
   }
}
