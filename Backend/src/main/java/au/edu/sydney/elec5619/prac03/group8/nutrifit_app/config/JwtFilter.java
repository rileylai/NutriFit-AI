package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.config;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth.JWTService;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JWTService jwtService;

    final ApplicationContext context;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 跳过OPTIONS预检请求
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            log.debug("Skipping JWT validation for OPTIONS request to {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String uuid = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                uuid = jwtService.extractUuid(token);
                log.debug("JWT filter processing request to {} for UUID: {}", requestUri, uuid);
            } catch (Exception e) {
                log.warn("⚠️ 401 Unauthorized - Invalid token format for request: {} from IP: {}",
                        requestUri, request.getRemoteAddr());
            }
        } else {
            log.trace("⚠️ 401 Unauthorized - No Bearer token found in request to {}", requestUri);
        }

        if (uuid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = context.getBean(MyUserDetailService.class).loadUserByUsername(uuid);
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication successful for UUID: {} accessing {}", uuid, requestUri);
                } else {
                    log.warn("Token validation failed for UUID: {} accessing {}", uuid, requestUri);
                    log.warn("⚠️ 401 Unauthorized - Token validation failed for UUID: {} on request: {} from IP: {}",
                            uuid, requestUri, request.getRemoteAddr());
                }
            } catch (Exception e) {
                log.error("Error during authentication for UUID: {} accessing {}: {}", uuid, requestUri, e.getMessage());
                log.error("⚠️ 401 Unauthorized - Authentication error for UUID: {} on request: {} from IP: {}, Error: {}",
                        uuid, requestUri, request.getRemoteAddr(), e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
