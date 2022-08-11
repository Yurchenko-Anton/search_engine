package com.example.controllers;

import com.example.controllers.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class DefaultController {
   private final PageService pageService;
@Autowired
    public DefaultController(PageService pageService) {
        this.pageService = pageService;
    }
    @RequestMapping("/")
    public String index(){
        return "index";
    }

}
