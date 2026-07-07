package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.dto.LoginRequest;
import com.dossa.ArtisanVault.project.dto.LoginResponse;
import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.service.ArtistaService;
import com.dossa.ArtisanVault.project.service.ClienteService;
import com.dossa.ArtisanVault.project.service.LoginRateLimiterService;
import com.dossa.ArtisanVault.project.service.LoginService;
import com.dossa.ArtisanVault.project.security.JwtService;
import com.dossa.ArtisanVault.project.security.TokenBlocklistService;
import com.dossa.ArtisanVault.project.security.TokenResolver;
import com.dossa.ArtisanVault.project.util.EmailNormalizer;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/login")
public class LoginController {
    public static final String COOKIE_NAME = "artisanvault_token";

    @Autowired
    private LoginService loginService;

    @Autowired
    private LoginRateLimiterService rateLimiterService;

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenBlocklistService tokenBlocklistService;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.trust-proxy-headers:false}")
    private boolean trustProxyHeaders;

    @PostMapping
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest.getEmail() == null || loginRequest.getSenha() == null) {
            return ResponseEntity.badRequest().body("Email e senha são obrigatórios.");
        }

        String email = EmailNormalizer.normalize(loginRequest.getEmail());
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
                    .sameSite(cookieSameSite)
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

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        String email = authentication.getName();
        boolean isArtista = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ARTISTA"));

        if (isArtista) {
            Optional<Artista> artista = artistaService.findByEmail(email);
            return artista
                    .<ResponseEntity<?>>map(a -> ResponseEntity.ok(new LoginResponse(a.getEmail(), "ARTISTA", a.getIdArtista(), a.getNome(), null)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artista não encontrado."));
        }

        Optional<Cliente> cliente = clienteService.findByEmail(email);
        return cliente
                .<ResponseEntity<?>>map(c -> ResponseEntity.ok(new LoginResponse(c.getEmail(), "CLIENTE", c.getIdCliente(), c.getNome(), null)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        blacklistCurrentToken(request);

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

    // O logout so limpa o cookie no navegador - o JWT em si continua
    // criptograficamente valido ate expirar (stateless), entao alguem com o
    // token capturado (via Authorization header) poderia reusa-lo mesmo apos o
    // "logout". Bloquear o jti ate a expiracao original do token fecha essa janela.
    private void blacklistCurrentToken(HttpServletRequest request) {
        String token = TokenResolver.resolve(request);
        if (token == null || !jwtService.isTokenValid(token)) {
            return;
        }
        Claims claims = jwtService.parseClaims(token);
        String jti = claims.getId();
        if (jti != null && claims.getExpiration() != null) {
            tokenBlocklistService.blacklist(jti, claims.getExpiration().getTime());
        }
    }

    private String clientIp(HttpServletRequest request) {
        if (trustProxyHeaders) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
