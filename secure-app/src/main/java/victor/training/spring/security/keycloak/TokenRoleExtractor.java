package victor.training.spring.security.keycloak;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import victor.training.spring.web.entity.UserRole;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toUnmodifiableSet;

@Slf4j
@Data
@Component
@Profile({"keycloak-be", "keycloak-fe"})
@ConfigurationProperties(prefix = "keycloak.roles")
public class TokenRoleExtractor implements GrantedAuthoritiesMapper {

  private String applicationName;
  private boolean expand = false;


  // ============== OAuth2 Login (OIDC) flow ==============
  @Override
  public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
    if (authorities.isEmpty()) {
      throw new IllegalArgumentException("No OIDC authorities => no way to get to OIDC Token");
    }
    GrantedAuthority firstAuthority = authorities.iterator().next();

    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) firstAuthority;

    ClaimAccessor idToken = oidcUserAuthority.getIdToken();

    List<String> tokenRoles = isApplicationLevel()
        ? getApplicationLevelRoles(idToken)
        : getRealmLevelRoles(idToken);

    return mapRolesToAuthorities(tokenRoles);
  }

  // ============== JWT Resource Server flow ==============
  /**
   * Extract roles from JWT access token for Resource Server authentication.
   * Both Jwt and OidcIdToken implement ClaimAccessor, so we can reuse the same extraction methods!
   */
  @SuppressWarnings("unchecked")
  public Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<String> tokenRoles = isApplicationLevel()
        ? getApplicationLevelRoles(jwt)
        : getRealmLevelRoles(jwt);

    return (Collection<GrantedAuthority>) mapRolesToAuthorities(tokenRoles);
  }

  // ============== Level determination ==============
  private boolean isApplicationLevel() {
    return Optional.ofNullable(applicationName)
        .filter(name -> !name.isBlank())
        .isPresent();
  }

  // ============== Common logic ==============
  private Collection<? extends GrantedAuthority> mapRolesToAuthorities(List<String> tokenRoles) {
    List<String> localRoles;
    if (expand) {
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
  private List<String> getRealmLevelRoles(ClaimAccessor token) {
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

  private List<String> getApplicationLevelRoles(ClaimAccessor token) {
    Map<String, Map<String, List<String>>> resourceAccess = token.getClaim("resource_access");
    if (resourceAccess == null) {
      throw new BadCredentialsException("User is not authorized on this application - No resource_access claim in token");
    }
    Map<String, List<String>> applicationAccess = resourceAccess.get(applicationName);
    if (applicationAccess == null) {
      throw new BadCredentialsException("User is not authorized on this application - No resource_access['%s'] claim in token".formatted(applicationName));
    }
    if (!applicationAccess.containsKey("roles")) {
      throw new BadCredentialsException("User is not authorized on this application - No roles in resource_access['%s'] claim in token".formatted(applicationName));
    }
    return applicationAccess.get("roles");
  }
}
