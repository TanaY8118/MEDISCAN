package com.mediscan.common.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Utility to execute internal background tasks with a formalized SYSTEM
 * principal.
 * This preserves the SecurityContext invariant utilized across services
 * while securely segregating user-initiated requests from system-initiated
 * jobs.
 */
@Component
public class SystemContextWrapper {

    private static final String SYSTEM_USER = "SYSTEM";
    private static final String ROLE_SYSTEM = "ROLE_SYSTEM";

    /**
     * Executes the provided Runnable within the context of the SYSTEM principal.
     * The original context (if any) is restored after execution to prevent leakage.
     *
     * @param task The task to execute.
     */
    public void executeAsSystem(Runnable task) {
        SecurityContext originalContext = SecurityContextHolder.getContext();
        try {
            SecurityContext systemContext = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken systemAuth = new UsernamePasswordAuthenticationToken(
                    SYSTEM_USER,
                    "N/A",
                    Collections.singletonList(new SimpleGrantedAuthority(ROLE_SYSTEM)));
            systemContext.setAuthentication(systemAuth);
            SecurityContextHolder.setContext(systemContext);

            task.run();
        } finally {
            SecurityContextHolder.setContext(originalContext);
        }
    }
}
