package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.dto.LoginRequest;
import com.dossa.ArtisanVault.project.dto.LoginResponse;
import com.dossa.ArtisanVault.project.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            String userType = loginService.login(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(new LoginResponse(loginRequest.getEmail(), userType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
