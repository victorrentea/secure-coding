package victor.training.spring.web.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.security.keycloak.TokenUtils;

import static org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal;

@RequiredArgsConstructor
@Component
@Slf4j
public class TeacherBioClient {
  @Value("${jwt.signature.shared.secret.base64}")
  private final String jwtSecret;
  @Value("${teacher.bio.uri.base}")
  private final String teacherBioUriBase;

  private final TeacherBioFeignClient feignClient;

  // don't do "new RestTemplate()" but take it from Spring, to allow Sleuth to propagate 'traceId'
  private final RestTemplate rest;
  private final RestClient restClient;


  // TODO cacheable
  public String retrieveBiographyForTeacher(long teacherId) {
    log.debug("Calling external web endpoint... (takes time)");
//    String result = dummyCall(teacherId);
//    String result = callUsingFeignClient(teacherId);
    String result = callUsingRestTemplate(teacherId);
    log.debug("Got result");
    return result;
  }


  @SneakyThrows
  public String callUsingFeignClient(long teacherId) {
    return feignClient.registerSheep(teacherId);
  }

  @SneakyThrows
  public String callUsingRestTemplate(long teacherId) {

    // #1 :) - no bearer
//    String token = "joke";

//     #2 (WIP) - using Refresh Token if user logged in in this app
    var token = relayUserAccessToken();

    // #3 - Client-Credentials
//    var token = clientCredentials();

    // #4 - Custom-JWT
//              String token = Jwts.builder()
//                    .setSubject(SecurityContextHolder.getContext().getAuthentication().getName())
//                    .claim("country", "Country")
//                    .signWith(SignatureAlgorithm.HS512, jwtSecret)
//                    .compact();


    return restClient.get()
        .uri(teacherBioUriBase + "/api/teachers/" + teacherId + "/bio")
        .headers(h->h.setBearerAuth(token))
//        .attributes(clientRegistrationId("client-credential"))
//        .attributes(principal("my-application"))
        .retrieve()
        .body(String.class);
  }

  private String relayUserAccessToken() {
    String token = TokenUtils.getCurrentToken().orElseThrow();
    log.info("Sending user JWT bearer: {}", token);
    return token;
  }

//  private String clientCredentials() {
//    var request = OAuth2AuthorizeRequest
//        .withClientRegistrationId("client-credential")
//        .principal("my-application")
//        .build();
//    var client = oAuth2AuthorizedClientManager.authorize(request); // <-- refreshes AT if expired
//    var token = client.getAccessToken().getTokenValue();
//    log.info("Using access token (exp:{}): {}",
//        client.getAccessToken().getExpiresAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
//        token);
//    return token;
//  }

}

