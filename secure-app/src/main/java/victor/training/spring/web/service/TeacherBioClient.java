package victor.training.spring.web.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.security.keycloak.ClientCredentialProvider;
import victor.training.spring.security.keycloak.TokenUtils;

import java.time.ZoneId;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class TeacherBioClient {
  @Value("${teacher.bio.uri.base}")
  private final String teacherBioUriBase;

  private final RestClient restClient;
  private final Optional<ClientCredentialProvider> clientCredentialProvider;

  public String retrieveBiographyForTeacher(long teacherId) {
    log.debug("Calling external API... ");

//    var token = clientCredentialProvider.orElseThrow().getClientCredentialsToken();
    // a) Client-Credential - get Access Token for this app
    // - Only option for non HTTP flows: startup or @Scheduler tasks, @KafkaListener....

    // b) Propagate user Access Token (from BE or FE)
    var token = TokenUtils.getCurrentToken().orElseThrow();

    log.info("Sending AccessToken: {}", token);

    return restClient.get()
        .uri(teacherBioUriBase + "/api/teachers/" + teacherId + "/bio")
        .headers(h -> h.setBearerAuth(token))
        .retrieve()
        .body(String.class);
  }
}

