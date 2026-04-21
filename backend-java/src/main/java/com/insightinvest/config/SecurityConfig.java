package com.insightinvest.config;

import com.insightinvest.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/forecast/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/screener/trending").permitAll()
                        
                        // User endpoints - require INVESTOR role
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("INVESTOR")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasRole("INVESTOR")
                        .requestMatchers(HttpMethod.GET, "/portfolios/**").hasRole("INVESTOR")
                        .requestMatchers(HttpMethod.POST, "/portfolios/**").hasRole("INVESTOR")
                        .requestMatchers(HttpMethod.PUT, "/portfolios/**").hasRole("INVESTOR")
                        .requestMatchers(HttpMethod.DELETE, "/portfolios/**").hasRole("INVESTOR")
                        .requestMatchers(HttpMethod.GET, "/watchlist/**").hasRole("INVESTOR")
                        .requestMatchers(HttpMethod.POST, "/watchlist/**").hasRole("INVESTOR")
                        .requestMatchers(HttpMethod.DELETE, "/watchlist/**").hasRole("INVESTOR")
                        .requestMatchers(HttpMethod.GET, "/reports/**").hasRole("INVESTOR")
                        
                        // Admin endpoints - require ADMIN role
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
