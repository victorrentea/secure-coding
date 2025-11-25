package victor.training.spring.security.config.userpass;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

import static org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse;

@Slf4j
@Profile("userpass")
@Configuration
@EnableWebSecurity // (debug = true) // see the filter chain in use
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class UserPassSecurityConfig {
  @PostConstruct
  public void hi() {
    log.warn("Using");
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()); // OK if I only expose REST APIs

//    http.csrf(csrf -> csrf //In case I take <form> post (eg JSP)
//        .csrfTokenRepository(withHttpOnlyFalse())
//        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));

    // adds a http filter that responds to Bro CORS preflight
     http.cors(Customizer.withDefaults()); // only if .js files come from a CDN (by default CORS requests get blocked)

    http.authorizeHttpRequests(authz -> authz
        //❌EVITA: .requestMatchers(HttpMethod.DELETE, "/api/trainings/*").hasRole("ADMIN")
        .anyRequest().authenticated()
    );

    http.formLogin(Customizer.withDefaults()) // display a login page
        .userDetailsService(userDetailsService()); // distinguish vs Actuator user/pass

    http.httpBasic(Customizer.withDefaults()) // also accept Authorization: Basic ... request header
        .userDetailsService(userDetailsService()); // distinguish vs Actuator user/pass

    return http.build();
  }

  // *** Dummy users with plain text passwords - NEVER USE IN PRODUCTION
  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails user = User.withDefaultPasswordEncoder()
        .username("user").password("user").roles("USER").build();
    UserDetails admin = User.withDefaultPasswordEncoder()
        .username("admin").password("admin").roles("ADMIN","TRAINING_DELETE").build();
    UserDetails power = User.withDefaultPasswordEncoder()
        .username("power").password("power").roles("POWER").build();
    return new InMemoryUserDetailsManager(user, admin, power);
  }

}
