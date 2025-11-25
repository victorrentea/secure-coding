package victor.training.spring.security.preauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class PreAuthFilter extends AbstractPreAuthenticatedProcessingFilter {
    public PreAuthFilter(AuthenticationManager authenticationManager) {
        setAuthenticationManager(requireNonNull(authenticationManager));
    }
    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest httpRequest) {
        String username = httpRequest.getHeader("x-user");
        String rolesCsv = httpRequest.getHeader("x-user-roles");
        if (username == null || username.isBlank() || rolesCsv == null || rolesCsv.isBlank()) {
            log.error("'x-user' or 'x-user-roles' NOT found in request headers");
            return null;
        }
        List<String> roles = List.of(rolesCsv.split(","));

//        roles = UserRole.expandToSubRoles(roles);

        return new PreAuthPrincipal(username, roles);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "null";
    }
}
