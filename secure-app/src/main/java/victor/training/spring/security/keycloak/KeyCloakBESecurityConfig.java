package victor.training.spring.security.keycloak;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Slf4j
@Profile("keycloak-be")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
class KeyCloakBESecurityConfig {
  @PostConstruct
  public void hi() {
    log.warn("Using config");
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(CsrfConfigurer::disable);
    http.authorizeHttpRequests(requests -> requests.anyRequest().authenticated());
    http.oauth2Login(login -> login.failureHandler(new LoginFailureHandler()));
    return http.build();
  }

  @EventListener
  public void onLogin(AuthenticationSuccessEvent event) {
    TokenUtils.printTheTokens(event.getAuthentication());
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

  private static class LoginFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse rsp, AuthenticationException ex) throws IOException, ServletException {
      if (ex instanceof BadCredentialsException) {
        rsp.setStatus(403);
        rsp.getWriter().write("Login failed: " + ex.getMessage());
      } else {
        throw ex;
      }
    }
  }
}
