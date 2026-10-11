
package com.securebank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/v1/hello")
    public String hello() {
        return "Welcome to SecureBank!";
    }
}
