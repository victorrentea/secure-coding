package victor.training.spring.security.keycloak;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class TokenUtils {
  public static void printTheTokens() {
    printTheTokens(SecurityContextHolder.getContext().getAuthentication());
  }
  public static void printTheTokens(Authentication authentication) {
    Object principal = authentication.getPrincipal();
    if (principal instanceof DefaultOidcUser oidcUser) {
      log.info("\n-- OpenID Connect Token: {} ", oidcUser.getUserInfo().getClaims());
      logAccessToken(oidcUser.getExpiresAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
          getCurrentToken().orElse("N/A"),
          oidcUser.getAttributes());
      return;
    }

    if (principal instanceof Jwt jwt) {
      LocalDateTime expirationTime = jwt.getExpiresAt() != null
          ? jwt.getExpiresAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
          : null;
      logAccessToken(expirationTime, jwt.getTokenValue(), jwt.getClaims());
      return;
    }

    log.debug("No OAuth Tokens. Principal=" + principal.getClass());
  }

  public static void logAccessToken(LocalDateTime expirationTime, String tokenValue, Map<String, Object> claims) {
    String deltaLeft = expirationTime != null && expirationTime.isAfter(LocalDateTime.now())
        ? "expires in " + LocalTime.MIN.plusSeconds(LocalDateTime.now().until(expirationTime, ChronoUnit.SECONDS)).toString()
        : "!EXPIRED!";
    log.info("\n-- Access Token 👑 ({} at {} local time): {}\n{}",
        deltaLeft,
        expirationTime,
        tokenValue,
        mapToPrettyJson(claims));
  }

  public static Optional<String> getCurrentToken() {
    SecurityContext context = SecurityContextHolder.getContext();
    if (context == null) {
      return Optional.empty();
    }
    Authentication authentication = context.getAuthentication();
    if (authentication == null) {
      return Optional.empty();
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof OidcUser oidcUser) {
      OidcIdToken idToken = oidcUser.getIdToken();
      if (idToken == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(idToken.getTokenValue());
    }
    if (principal instanceof Jwt jwt) {
      return Optional.of(jwt.getTokenValue());
    }
    return Optional.empty();
  }

  private static String mapToPrettyJson(Map<String, Object> map) {
    return map.entrySet().stream().sorted(Map.Entry.comparingByKey())
        .map(e -> "\t" + e.getKey() + ": " + e.getValue())
        .collect(Collectors.joining("\n"));
  }

  private List<String> extractAuthoritiesAfterKeycloakAuthn() {
    TokenUtils.printTheTokens();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//    KeycloakPrincipal<KeycloakSecurityContext> keycloakToken =(KeycloakPrincipal<KeycloakSecurityContext>) authentication.getPrincipal();
//    log.info("Other details about user from ID Token: " + keycloakToken.getKeycloakSecurityContext().getIdToken().getOtherClaims());
//    return keycloakToken.getSubRoles();
    return List.of();
  }

}
