package victor.training.spring.security.preauth;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

// this will allow going in just using headers, eg:
// curl http://localhost:8080/api/trainings -H 'X-User: user' -H 'X-User-Roles: USER'
@Slf4j
@Profile("preauth")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class PreAuthSecurityConfig {
  @PostConstruct
  public void hi() {
    log.warn("Using config");
  }

  @Bean
  @Order(2)
  public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {
    http.csrf(csrf ->csrf.disable());
    http.authorizeHttpRequests(authz -> authz.anyRequest().authenticated());
    http.addFilter(new PreAuthFilter(authenticationConfiguration.getAuthenticationManager()));
    http.authenticationProvider(preAuthenticatedProvider());
    http.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // don't emit Set-Cookie
    return http.build();
  }

  @Bean
  public AuthenticationProvider preAuthenticatedProvider() {
    PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
    provider.setPreAuthenticatedUserDetailsService(token -> (PreAuthPrincipal) token.getPrincipal());
    return provider;
  }


  // i'm sorry: see https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter#accessing-the-local-authenticationmanager

}
