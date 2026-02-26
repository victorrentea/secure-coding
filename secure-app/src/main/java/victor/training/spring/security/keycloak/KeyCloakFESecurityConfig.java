package victor.training.spring.security.keycloak;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static victor.training.spring.security.keycloak.TokenRolesToLocalRoles.RoleLevel.CLIENT_LEVEL;

@Slf4j
@Profile("keycloak-fe")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
class KeyCloakFESecurityConfig {
  @PostConstruct
  public void hi() {
    log.warn("Using config");
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(CsrfConfigurer::disable);
    http.authorizeHttpRequests(requests -> requests
        .requestMatchers("/*").permitAll() // access static files (.html, .js ...)
        .anyRequest().authenticated()
    );
    http.exceptionHandling(config -> config
        .defaultAuthenticationEntryPointFor(
            new HttpStatusEntryPoint(HttpStatus.FORBIDDEN),
            request -> request.getRequestURI().startsWith("/api/")
        )
    );
    // resource server: only consumer AcessTokens
    http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    // stateless api (don't create sessions)
    http.sessionManagement(config -> config.sessionCreationPolicy(STATELESS));
    return http.build();
  }

  @Bean
  public GrantedAuthoritiesMapper extractAuthoritiesFromToken() {
    return new TokenRolesToLocalRoles(CLIENT_LEVEL, false);
  }

  @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
  private String issuerUri;

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
  }

}
