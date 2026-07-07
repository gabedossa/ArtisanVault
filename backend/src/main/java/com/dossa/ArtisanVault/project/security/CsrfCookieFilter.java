package com.dossa.ArtisanVault.project.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Forca a resolucao "preguicosa" do CsrfToken em toda requisicao, o que faz o
 * CookieCsrfTokenRepository efetivamente gravar o cookie XSRF-TOKEN na resposta.
 * Sem isso, o cookie so seria criado quando algo chamasse token.getToken()
 * explicitamente, o que nao acontece por padrao numa API stateless para SPA.
 */
@Component
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
