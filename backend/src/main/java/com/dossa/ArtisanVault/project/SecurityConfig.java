package com.dossa.ArtisanVault.project;

import com.dossa.ArtisanVault.project.security.CsrfCookieFilter;
import com.dossa.ArtisanVault.project.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CsrfCookieFilter csrfCookieFilter;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    // "Lax" funciona quando frontend e backend sao same-site (ex.: localhost em
    // dev). Em producao, se frontend e backend ficam em dominios diferentes
    // (ex.: Vercel + Railway), o navegador so envia o cookie em requisicoes
    // cross-site se for "None" - o que exige Secure=true (HTTPS).
    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    // Lista separada por virgula (ex.: "https://meuapp.vercel.app,https://www.meudominio.com").
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie.secure(cookieSecure).sameSite(cookieSameSite));

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        // Login nao opera sobre uma sessao ja autenticada (nao ha
                        // autoridade ambiente para um CSRF abusar), entao fica de fora
                        // para nao depender de o cookie CSRF ja existir na primeira visita.
                        .ignoringRequestMatchers("/api/login", "/api/login/logout")
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/artistas/email").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/api/artistas", "/api/artistas/*",
                                "/api/servico", "/api/servico/*",
                                "/api/portifolio", "/api/portifolio/*",
                                "/uploads/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login", "/api/login/logout", "/api/cliente/post", "/api/artistas").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(csrfCookieFilter, BasicAuthenticationFilter.class);

        return http.build();
    }
}
