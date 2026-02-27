package victor.training.spring.security.keycloak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("keycloak-cc")
public class ClientCredentialProvider {
  private final OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

  public String getClientCredentialsToken() {
    var request = OAuth2AuthorizeRequest
        .withClientRegistrationId("client-credential")
        .principal("my-application")
        .build();
    var client = oAuth2AuthorizedClientManager.authorize(request); // <-- refreshes AT if it's expired
    if (client == null) {
      throw new IllegalStateException("Failed to authorize client with registration ID 'client-credential'. " +
          "Check that the client registration exists in application properties and OAuth2AuthorizedClientManager is properly configured.");
    }
    var token = client.getAccessToken().getTokenValue();
    log.info("Using access token (exp:{}): {}",
        client.getAccessToken().getExpiresAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
        token);
    return token;
  }

}
