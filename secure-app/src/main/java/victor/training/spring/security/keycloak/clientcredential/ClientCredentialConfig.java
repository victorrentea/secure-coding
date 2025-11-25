//package victor.training.spring.security.config.keycloak.clientcredential;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.*;
//import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
//
//@Configuration
//public class ClientCredentialConfig {
//
////  @Bean
////  public OAuth2AuthorizedClientManager authorizedClientManager(
////      ClientRegistrationRepository clientRegistrationRepository,
////      OAuth2AuthorizedClientService authorizedClientRepository) {
////
////    OAuth2AuthorizedClientProvider authorizedClientProvider =
////        OAuth2AuthorizedClientProviderBuilder.builder()
////            .authorizationCode()
////            .refreshToken()
////            .clientCredentials()
////            .build();
////
////    AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
////        new AuthorizedClientServiceOAuth2AuthorizedClientManager(
////            clientRegistrationRepository, authorizedClientRepository);
////    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
////
////    return authorizedClientManager;
////  }
//
//  @Bean
//  public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager(
//      ClientRegistrationRepository clientRegistrationRepository,
//      OAuth2AuthorizedClientService authorizedClientService) {
//
//    OAuth2AuthorizedClientProvider authorizedClientProvider =
//        OAuth2AuthorizedClientProviderBuilder.builder()
//            .clientCredentials()
//            .build();
//
//    AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
//        new AuthorizedClientServiceOAuth2AuthorizedClientManager(
//            clientRegistrationRepository, authorizedClientService);
//    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
//
//    return authorizedClientManager;
//  }
//}
