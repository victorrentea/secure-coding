package victor.training.spring.security.keycloak;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

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
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
    http.csrf(CsrfConfigurer::disable);
    http.authorizeHttpRequests(requests -> requests
        .requestMatchers("/*").permitAll() // allow access to SPA static files: .html, .js ...
        .anyRequest().authenticated()
    );
    // resource server: only consumer AcessTokens
    http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
    // stateless api (don't create sessions)
    http.sessionManagement(config -> config.sessionCreationPolicy(STATELESS));
    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}") String issuerUri) {
    return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter(TokenRoleExtractor tokenRoleExtractor) {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    // 🔑 Set the claim name to extract the principal (username) from
    converter.setPrincipalClaimName("preferred_username");

    // 🎭 Reuse TokenRoleExtractor to extract roles from token
    converter.setJwtGrantedAuthoritiesConverter(tokenRoleExtractor::extractAuthorities);

    return converter;
  }

}
