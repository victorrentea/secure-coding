package victor.training.spring.security.keycloak;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimAccessor;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import victor.training.spring.web.entity.UserRole;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Slf4j
public class TokenRolesToLocalRoles implements GrantedAuthoritiesMapper {
  public enum RoleLevel {
    /** Global per ecosystem, in OIDCToken.realm_access.roles */
    REALM_LEVEL,
    /** Specific to my system, in OIDCToken.resource_access['spring-app'].roles */
    CLIENT_LEVEL
  }

  private final boolean expandRoles;
  private final RoleLevel roleLevel;

  public TokenRolesToLocalRoles(RoleLevel roleLevel, boolean expandRoles) {
    this.roleLevel = roleLevel;
    this.expandRoles = expandRoles;
  }

  @Value("${spring.application.name}")
  private String clientId;

  @Override
  public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
    if (authorities.isEmpty()) {
      throw new IllegalArgumentException("No OIDC authorities => no way to get to OIDC Token");
    }
    GrantedAuthority firstAuthority = authorities.iterator().next();

    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) firstAuthority;

    IdTokenClaimAccessor idToken = oidcUserAuthority.getIdToken();

    List<String> tokenRoles = switch (roleLevel) {
      case REALM_LEVEL -> getRealmGlobalRoles(idToken);
      case CLIENT_LEVEL -> getClientSpecificRoles(idToken);
    };

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

  private List<String> getRealmGlobalRoles(IdTokenClaimAccessor idToken) {
    Map<String, List<String>> realmAccess = idToken.getClaim("realm_access");
    if (realmAccess == null) {
      throw new BadCredentialsException("User is not authorized - No realm_access claim in OIDC Token");
    }
    List<String> roles = realmAccess.get("roles");
    if (roles == null) {
      throw new BadCredentialsException("User is not authorized - No roles in realm_access claim in OIDC Token");
    }
    return roles;
  }

  private List<String> getClientSpecificRoles(IdTokenClaimAccessor idToken) {
    Map<String, Map<String, List<String>>> resourceAccess = idToken.getClaim("resource_access");
    if (resourceAccess == null) {
      throw new BadCredentialsException("User is not authorized on this application - No resource_access claim in OIDC Token");
    }
    Map<String, List<String>> clientAccess = resourceAccess.get(clientId);
    if (clientAccess == null) {
      throw new BadCredentialsException("User is not authorized on this application - No resource_access['%s'] claim in OIDC Token".formatted(clientId));
    }
    if (!clientAccess.containsKey("roles")) {
      throw new BadCredentialsException("User is not authorized on this application - No roles in resource_access['%s'] claim in OIDC Token".formatted(clientId));
    }
    return clientAccess.get("roles");
  }
}
