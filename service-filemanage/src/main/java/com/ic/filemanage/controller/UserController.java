package com.ic.filemanage.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/User")
public class UserController {
    @RequestMapping(value = "/fuzzySearch", method = RequestMethod.GET)
    public String fuzzySearch(String keyword) {
        return "test";
    }

}
