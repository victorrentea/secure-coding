//package victor.training.spring.security.config.keycloak.clientcredential;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.client.*;
//import org.springframework.security.oauth2.core.OAuth2AccessToken;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class OAuth2ClientController {
//
//  @Autowired
//  private OAuth2AuthorizedClientService authorizedClientService;
//  @Autowired
//  private AuthorizedClientServiceOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
//
//
//  @GetMapping("/client-credential")
//  public String index() {
//    OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
//        .withClientRegistrationId("client-credential")
//        .principal("my-app")
//        .build();
//    OAuth2AuthorizedClient authorizedClient = oAuth2AuthorizedClientManager.authorize(authorizeRequest);
////    OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("client-credential", authentication.getName());
//
//    OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
//
//
//    return accessToken.getTokenValue();
//  }
//}