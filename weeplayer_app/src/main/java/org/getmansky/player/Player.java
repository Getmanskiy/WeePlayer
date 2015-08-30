package org.getmansky.player;

import javafx.util.Duration;
import org.getmansky.model.Playlist;
import org.getmansky.model.Track;
import org.getmansky.util.EventHandler;

/**
 * @author getmansky
 */
public interface Player {

    public void setData(Playlist playlist, Track track);

    public void togglePause();

    public void setMode(PlayerMode mode);

    public void setVolume(Double volume);

    public void decidePlayNext();

    public void seek(Duration duration);

    public void play();

    public void playPrevious();

    public void setOnData(PlayerDataHandler handler);

    public void setOnVolume(EventHandler<Double> handler);

    public void setOnMode(EventHandler<Double> handler);

    public void setOnSeek(EventHandler<Duration> handler);

    public void setOnTogglePause(EventHandler<Boolean> handler);

    public void setOnPlay(EventHandler<Track> handler);
}
