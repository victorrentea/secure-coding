package victor.training.spring.security.apikey;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Setter
@Getter
public class ApiKeyFilter extends AbstractPreAuthenticatedProcessingFilter {
  private String globalKey;
  private Map<String,String> clientKeys;

  public ApiKeyFilter() {
    setAuthenticationManager(this::acceptApiKeyUser);
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest httpRequest) {
    String apiKeyHeader = requireNonNull(httpRequest.getHeader("x-api-key"), "Missing header x-api-key");
    if (globalKey.equals(apiKeyHeader)) {
      return apiKeyHeader;
    }
    // TODO better: use 1 api-key / client app:
    // 1) client compromised / no longer calls me => can selectively invalidate its key
    // 2) authorization: can assign different permissions
    // 3) resource abuse/fairness: can set rate limiter per client
    Optional<String> clientIdOpt = clientKeys.entrySet().stream()
        .filter(e -> Objects.equals(e.getValue(), apiKeyHeader))
        .findFirst()
        .map(Map.Entry::getKey);
    if (clientIdOpt.isPresent()) {
      return clientIdOpt.get();
    }
    return null;
  }

  private Authentication acceptApiKeyUser(Authentication authentication) {
    if (authentication.getPrincipal() instanceof String) {
      authentication.setAuthenticated(true);
      return authentication;
    }
    throw new BadCredentialsException("Missing or incorrect api key: " + authentication.getPrincipal());
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }
}
