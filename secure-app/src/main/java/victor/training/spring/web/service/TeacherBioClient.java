package victor.training.spring.web.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;

@RequiredArgsConstructor
@Component
@Slf4j
public class TeacherBioClient {
  @Value("${jwt.signature.shared.secret.base64}")
  private final String jwtSecret;
  @Value("${teacher.bio.uri.base}")
  private final String teacherBioUriBase;

  // don't do "new RestTemplate()" but take it from Spring, to allow Sleuth to propagate 'traceId'
  private final RestTemplate rest;
  private final RestClient restClient;

  public String retrieveBiographyForTeacher(long teacherId) {
    log.debug("Calling external API... ");

    // #1 :) - no bearer
//    String token = "joke";

//     #2 Propagate user Access Token
//    var token = TokenUtils.getCurrentToken().orElseThrow();

    // #3 Client-Credentials - login this app to KeyCloak)
    var token = getClientCredentialsToken();
    log.info("Sending JWT AT: {}", token);

    return restClient.get()
        .uri(teacherBioUriBase + "/api/teachers/" + teacherId + "/bio")
        .headers(h -> h.setBearerAuth(token))
        .retrieve()
        .body(String.class);
  }

  private final OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

  private String getClientCredentialsToken() {
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

