package victor.training.spring.security.keycloak;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import victor.training.spring.web.entity.UserRole;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toUnmodifiableSet;

@Slf4j
public class ExtractRolesFromToken implements GrantedAuthoritiesMapper {
  public enum RoleLevel {
    /** Global per ecosystem, in OIDCToken.realm_access.roles */
    REALM_LEVEL,
    /** Specific to my system, in OIDCToken.resource_access['spring-app'].roles */
    APPLICATION_LEVEL
  }

  private final boolean expandRoles;
  private final RoleLevel roleLevel;

  public ExtractRolesFromToken(RoleLevel roleLevel, boolean expandRoles) {
    this.roleLevel = roleLevel;
    this.expandRoles = expandRoles;
  }

  @Value("${spring.application.name}")
  private String applicationName;

  // ============== OAuth2 Login (OIDC) flow ==============
  @Override
  public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
    if (authorities.isEmpty()) {
      throw new IllegalArgumentException("No OIDC authorities => no way to get to OIDC Token");
    }
    GrantedAuthority firstAuthority = authorities.iterator().next();

    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) firstAuthority;

    ClaimAccessor idToken = oidcUserAuthority.getIdToken();

    List<String> tokenRoles = switch (roleLevel) {
      case REALM_LEVEL -> getRealmGlobalRoles(idToken);
      case APPLICATION_LEVEL -> getClientSpecificRoles(idToken);
    };

    return mapRolesToAuthorities(tokenRoles);
  }

  // ============== JWT Resource Server flow ==============
  /**
   * Extract roles from JWT access token for Resource Server authentication.
   * Both Jwt and OidcIdToken implement ClaimAccessor, so we can reuse the same extraction methods!
   */
  @SuppressWarnings("unchecked")
  public Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<String> tokenRoles = switch (roleLevel) {
      case REALM_LEVEL -> getRealmGlobalRoles(jwt);
      case APPLICATION_LEVEL -> getClientSpecificRoles(jwt);
    };

    return (Collection<GrantedAuthority>) mapRolesToAuthorities(tokenRoles);
  }

  // ============== Common logic ==============
  private Collection<? extends GrantedAuthority> mapRolesToAuthorities(List<String> tokenRoles) {
    List<String> localRoles;
    if (expandRoles) {
      localRoles = UserRole.expandToSubRoles(tokenRoles);
      log.debug("Expanded roles: {}", tokenRoles);
    } else {
      localRoles = tokenRoles;
    }
    log.debug("Token roles: {} => Local roles: {} ", tokenRoles, localRoles);
    return localRoles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(toUnmodifiableSet());
  }

  // ============== Token claim extraction (works for both OIDC and JWT) ==============
  private List<String> getRealmGlobalRoles(ClaimAccessor token) {
    Map<String, List<String>> realmAccess = token.getClaim("realm_access");
    if (realmAccess == null) {
      throw new BadCredentialsException("User is not authorized - No realm_access claim in token");
    }
    List<String> roles = realmAccess.get("roles");
    if (roles == null) {
      throw new BadCredentialsException("User is not authorized - No roles in realm_access claim in token");
    }
    return roles;
  }

  private List<String> getClientSpecificRoles(ClaimAccessor token) {
    Map<String, Map<String, List<String>>> resourceAccess = token.getClaim("resource_access");
    if (resourceAccess == null) {
      throw new BadCredentialsException("User is not authorized on this application - No resource_access claim in token");
    }
    Map<String, List<String>> clientAccess = resourceAccess.get(applicationName);
    if (clientAccess == null) {
      throw new BadCredentialsException("User is not authorized on this application - No resource_access['%s'] claim in token".formatted(applicationName));
    }
    if (!clientAccess.containsKey("roles")) {
      throw new BadCredentialsException("User is not authorized on this application - No roles in resource_access['%s'] claim in token".formatted(applicationName));
    }
    return clientAccess.get("roles");
  }
}
