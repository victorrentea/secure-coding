package victor.training.micro.security.disabled;//package victor.training.micro.jwt;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Profile("!jwt & !keycloak")
@Configuration
@EnableWebSecurity
public class SecurityConfigDisabled {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
    return http.build();
  }

}
