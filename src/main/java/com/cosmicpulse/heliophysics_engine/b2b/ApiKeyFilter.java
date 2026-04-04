package com.cosmicpulse.heliophysics_engine.b2b;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class ApiKeyFilter implements Filter {

    // B2B API keys — in production these would be in a database
    private static final Set<String> VALID_B2B_KEYS = Set.of(
        "cp-b2b-demo-key-001",
        "cp-b2b-demo-key-002"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        // Only protect /api/b2b/* endpoints
        if (!path.startsWith("/api/b2b/")) {
            chain.doFilter(req, res);
            return;
        }

        String key = request.getHeader("X-API-Key");
        if (key == null) key = request.getParameter("api_key");

        if (key == null || !VALID_B2B_KEYS.contains(key)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {"error":"Unauthorized","message":"Valid X-API-Key header required","docs":"https://cosmicpulse.io/api/docs"}
                """);
            log.warn("Unauthorized B2B API access attempt from {}", request.getRemoteAddr());
            return;
        }

        chain.doFilter(req, res);
    }
}
