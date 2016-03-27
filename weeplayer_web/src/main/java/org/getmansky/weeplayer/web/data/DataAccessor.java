package org.getmansky.weeplayer.web.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.getmansky.model.Playlist;
import org.getmansky.model.Track;
import org.getmansky.weeplayer.web.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
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

    private Map<String, Playlist> fetchedPlaylists;
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
        List<Playlist> playlists = fetchPlaylists();
        fetchedPlaylists = new HashMap<>();
        playlists.forEach(playlist -> {
            List<Track> invertedTracks = new ArrayList<>();
            for(int i = playlist.getTracks().size() - 1; i >= 0; i--) {
                invertedTracks.add(playlist.getTracks().get(i));
            }
            playlist.setTracks(invertedTracks);
            fetchedPlaylists.put(playlist.getId(), playlist);
        });
        playlistsNoContent.clear();
        fetchedPlaylists.values().stream()
                .sorted((pl1, pl2) -> pl1.getTitle().compareTo(pl2.getTitle()))
                .forEach((Playlist fetchedPlaylist) -> {
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

    public Playlist getPlaylist(String id, Integer offset) {
        Playlist playlist = fetchedPlaylists.get(id);
        List<Track> tracks = playlist.getTracks();
        Playlist result = new Playlist();
        result.setId(id);
        result.setTitle(playlist.getTitle());
        result.setTracks(safeSublist(tracks, offset, 150));
        return result;
    }

    private static <T> List<T> safeSublist(List<T> list, int offset, int size) {
        if(offset > list.size()) return Collections.emptyList();
        if(offset + size > list.size()) {
            size = list.size() - offset;
        }
        return list.subList(offset, offset + size);
    }

    public Map<String, Playlist> getFetchedPlaylists() {
        return fetchedPlaylists;
    }

    public List<Playlist> getPlaylistsNoContent() {
        return playlistsNoContent;
    }
}
