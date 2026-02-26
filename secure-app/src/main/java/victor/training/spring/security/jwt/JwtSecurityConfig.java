package victor.training.spring.security.jwt;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

@Slf4j
@Profile("jwt")
@EnableWebSecurity
@Configuration
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class JwtSecurityConfig {
  @PostConstruct
  public void hi() {
    log.warn("Using config");
  }

  @Value("${jwt.signature.shared.secret.base64}")
  private String jwtSecret;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.authorizeHttpRequests(authz -> authz.anyRequest().authenticated());
    http.authenticationProvider(preAuthenticatedProvider());
    http.addFilter(new JwtFilter(authenticationConfiguration.getAuthenticationManager(), jwtSecret));
    http.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // don't emit Set-Cookie
    return http.build();
  }

  @Bean
  public AuthenticationProvider preAuthenticatedProvider() {
    PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
    provider.setPreAuthenticatedUserDetailsService(token -> (JwtPrincipal) token.getPrincipal());
    return provider;
  }

}
