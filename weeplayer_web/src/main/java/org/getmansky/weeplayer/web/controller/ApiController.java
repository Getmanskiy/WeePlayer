package org.getmansky.weeplayer.web.controller;

import org.apache.commons.io.IOUtils;
import org.getmansky.model.Playlist;
import org.getmansky.weeplayer.web.Application;
import org.getmansky.weeplayer.web.data.DataAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @RequestMapping("/tracks/{id}")
    @ResponseBody
    public ResponseEntity getFile(@PathVariable String id) throws IOException {
        ResponseEntity respEntity;

        id = id + ".mp3";
        File result = new File(System.getProperty(Application.STORAGE_PATH_KEY) + "/tracks/" + id);

        if(result.exists()){
            InputStream inputStream = new FileInputStream(result);
            String type=result.toURL().openConnection().guessContentTypeFromName(id);

            byte[] out = IOUtils.toByteArray(inputStream);

            HttpHeaders responseHeaders = new HttpHeaders();
//            responseHeaders.add("content-disposition", "attachment; filename=" + id);
//            responseHeaders.add("Content-Type",type);

            respEntity = new ResponseEntity(out, responseHeaders, HttpStatus.OK);
        }else{
            respEntity = new ResponseEntity ("File Not Found", HttpStatus.OK);
        }
        return respEntity;
    }
}
