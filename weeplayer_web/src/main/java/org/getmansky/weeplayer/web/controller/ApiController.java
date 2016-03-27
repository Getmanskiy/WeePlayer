package org.getmansky.weeplayer.web.controller;

import org.getmansky.model.Playlist;
import org.getmansky.weeplayer.web.data.DataAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by getmansky on 10.03.2016.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private DataAccessor dataAccessor;

    @RequestMapping("/playlists")
    public List<Playlist> playlists() {
        return dataAccessor.getPlaylistsNoContent();
    }

    @RequestMapping("/playlists/{id}")
    public Playlist playlist(@PathVariable String id) {
        return dataAccessor.getPlaylist(id);
    }
}
