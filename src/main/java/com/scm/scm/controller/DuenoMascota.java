package com.scm.scm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class DuenoMascota {
    @GetMapping("/dueno/index")
    public String duenoIndex() {
        return "duenoMascota/index";
    }
}

