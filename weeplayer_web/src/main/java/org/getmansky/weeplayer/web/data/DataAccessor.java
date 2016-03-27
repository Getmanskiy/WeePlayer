package org.getmansky.weeplayer.web.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.getmansky.model.Playlist;
import org.getmansky.weeplayer.web.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by getmansky on 27.03.2016.
 */
@Scope("singleton")
@Component
public class DataAccessor {

    private static final Logger log = LoggerFactory.getLogger(DataAccessor.class);

    private List<Playlist> fetchedPlaylists;
    private List<Playlist> playlistsNoContent = new ArrayList<>();

    @PostConstruct
    public void init() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(() -> {
            refreshState();
        }, 0, 60, TimeUnit.SECONDS);
    }

    private void refreshState() {
        log.info("Refreshing state");
        fetchedPlaylists = fetchPlaylists();
        playlistsNoContent.clear();
        fetchedPlaylists.forEach((Playlist fetchedPlaylist) -> {
            Playlist playlist = new Playlist();
            playlist.setId(fetchedPlaylist.getId());
            playlist.setTitle(fetchedPlaylist.getTitle());
            playlistsNoContent.add(playlist);
        });
        log.info("Done refreshing state");
    }

    private List<Playlist> fetchPlaylists() {
        Type listType = new TypeToken<List<Playlist>>() {}.getType();
        Gson gson = new Gson();
        try {
            BufferedReader br = new BufferedReader(new FileReader(System.getProperty(Application.STORAGE_PATH_KEY) + "/playlists/list.json"));
            List<Playlist> playlists = gson.fromJson(br, listType);
            return playlists;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Playlist> getFetchedPlaylists() {
        return fetchedPlaylists;
    }

    public List<Playlist> getPlaylistsNoContent() {
        return playlistsNoContent;
    }
}
