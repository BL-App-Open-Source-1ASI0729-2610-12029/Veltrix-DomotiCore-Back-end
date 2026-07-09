package com.domoticore.shared.infrastructure.security;

import com.domoticore.integrations.application.DeveloperApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class DeveloperApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String DEVELOPER_API_PREFIX = "/api/v1/developer";
    private static final String API_KEY_HEADER = "X-DomotiCore-Api-Key";

    private final DeveloperApiKeyService developerApiKeyService;

    public DeveloperApiKeyAuthenticationFilter(DeveloperApiKeyService developerApiKeyService) {
        this.developerApiKeyService = developerApiKeyService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(DEVELOPER_API_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        developerApiKeyService
                .extractApiKey(request.getHeader("Authorization"), request.getHeader(API_KEY_HEADER))
                .ifPresent(apiKey -> {
                    var user = developerApiKeyService.resolveUserByApiKey(apiKey);
                    var userDetails = new DomotiCoreUserDetails(user);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });

        filterChain.doFilter(request, response);
    }
}
