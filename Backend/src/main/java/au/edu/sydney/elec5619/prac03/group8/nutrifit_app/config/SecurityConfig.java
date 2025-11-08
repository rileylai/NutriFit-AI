package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(request -> request.getMethod().equals("OPTIONS")).permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            String authHeader = request.getHeader("Authorization");
            String method = request.getMethod();

            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("⚠️ 401 Unauthorized - No Authorization header | Method: {} | URI: {} | IP: {} | User-Agent: {} | Reason: {}",
                        method,
                        request.getRequestURI(),
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent"),
                        authException.getMessage());
            } else if (!authHeader.startsWith("Bearer ")) {
                log.warn("⚠️ 401 Unauthorized - Authorization header format error | Method: {} | URI: {} | IP: {} | Auth Header: {} | Reason: {}",
                        method,
                        request.getRequestURI(),
                        request.getRemoteAddr(),
                        authHeader.length() > 50 ? authHeader.substring(0, 50) + "..." : authHeader,
                        authException.getMessage());
            } else {
                String token = authHeader.substring(7);
                log.warn("⚠️ 401 Unauthorized - Token validate failed| Method: {} | URI: {} | IP: {} | Token first 20: {} | Reason: {}",
                        method,
                        request.getRequestURI(),
                        request.getRemoteAddr(),
                        token.length() > 20 ? token.substring(0, 20) + "..." : token,
                        authException.getMessage());
            }

            log.debug("request details - Headers:");
            request.getHeaderNames().asIterator().forEachRemaining(headerName ->
                log.debug("  {}: {}", headerName, request.getHeader(headerName))
            );

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            BCryptPasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) throws Exception {

        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);

        return authBuilder.build();
    }
}
