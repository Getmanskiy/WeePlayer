package org.getmansky.weeplayer.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by getmansky on 10.03.2016.
 */
@RestController
@RequestMapping("api")
public class ApiController {

    @RequestMapping("/hi")
    public String hi() {
        return "hi";
    }
}
