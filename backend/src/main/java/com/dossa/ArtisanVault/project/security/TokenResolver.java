package com.dossa.ArtisanVault.project.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

// Compartilhado entre JwtAuthenticationFilter (autenticar cada requisicao) e
// LoginController (logout precisa do token atual para invalida-lo).
public final class TokenResolver {

    private TokenResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("artisanvault_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
