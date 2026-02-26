package victor.training.spring.security.keycloak;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

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
        .requestMatchers("/*").permitAll() // allow access to SPA static files: .html, .js ...
        .anyRequest().authenticated()
    );
    // resource server: only consumer AcessTokens
    http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
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

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    // 🔑 Set the claim name to extract the principal (username) from
    // Common options:
    // - "preferred_username" (Keycloak default - recommended)
    // - "sub" (JWT standard subject claim)
    // - "email" (user's email)
    // - "name" (user's full name)
    // - any custom claim you've configured in Keycloak
    converter.setPrincipalClaimName("preferred_username");

    // Configure how to extract roles/authorities from the token
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    // authoritiesConverter.setAuthoritiesClaimName("roles"); // where roles are in token
    // authoritiesConverter.setAuthorityPrefix("ROLE_"); // prefix for Spring Security roles
    converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

    return converter;
  }

}
