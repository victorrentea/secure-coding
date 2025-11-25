package victor.training.spring.security.config.keycloak;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.client.RestClient;

import static victor.training.spring.security.config.keycloak.TokenRolesToLocalRoles.RoleLevel.CLIENT_LEVEL;

@Slf4j
@Profile("keycloak")
@Configuration
@EnableWebSecurity//(debug = true) // see the filter chain in use
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
class KeyCloakSecurityConfig {

  @PostConstruct
  public void hi() {
    log.warn("Using");
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(CsrfConfigurer::disable);
    http.authorizeHttpRequests(registry -> registry
        .anyRequest().authenticated()
    );
    http.oauth2Login(login -> login
        .failureHandler((req, rsp, ex) -> {
          if (ex instanceof BadCredentialsException) {
            rsp.setStatus(403);
            rsp.getWriter().write("Login failed: " + ex.getMessage());
          } else {
            throw ex;
          }
        }));
    return http.build();
  }

  @Bean
  @Primary
  public RestClient restClient(OAuth2AuthorizedClientManager authorizedClientManager) {
    var requestInterceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
    requestInterceptor.setPrincipalResolver(new RequestAttributePrincipalResolver());
    requestInterceptor.setClientRegistrationIdResolver(new RequestAttributeClientRegistrationIdResolver());

    return RestClient.builder()
        .requestFactory(new HttpComponentsClientHttpRequestFactory())
        .requestInterceptor(requestInterceptor)
        .build();
  }

  @Bean
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
  }

  @Bean
  public GrantedAuthoritiesMapper extractAuthoritiesFromToken() {
    return new TokenRolesToLocalRoles(CLIENT_LEVEL, false);
  }

}
