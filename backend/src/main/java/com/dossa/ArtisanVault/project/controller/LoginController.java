package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.dto.LoginRequest;
import com.dossa.ArtisanVault.project.dto.LoginResponse;
import com.dossa.ArtisanVault.project.service.LoginRateLimiterService;
import com.dossa.ArtisanVault.project.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {
    public static final String COOKIE_NAME = "artisanvault_token";

    @Autowired
    private LoginService loginService;

    @Autowired
    private LoginRateLimiterService rateLimiterService;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest.getEmail() == null || loginRequest.getSenha() == null) {
            return ResponseEntity.badRequest().body("Email e senha são obrigatórios.");
        }

        String email = loginRequest.getEmail().trim().toLowerCase();
        String ip = clientIp(request);

        if (rateLimiterService.isBlocked(email, ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Muitas tentativas de login. Tente novamente em alguns minutos.");
        }

        try {
            LoginResponse response = loginService.login(email, loginRequest.getSenha());
            rateLimiterService.reset(email, ip);

            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, response.getToken())
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(expirationMs / 1000)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        } catch (UsernameNotFoundException | BadCredentialsException exception) {
            rateLimiterService.registerAttempt(email, ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logout realizado com sucesso.");
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
