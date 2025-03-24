package com.tilguys.matilda.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/api/v1/user")
public class UserController {

    @GetMapping("/hi")
    public String hi() {
        return "";
    }

    public void signup() {

    }

    public void login() {

    }
}
