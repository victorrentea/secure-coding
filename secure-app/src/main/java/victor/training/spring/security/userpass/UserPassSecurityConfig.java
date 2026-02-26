package victor.training.spring.security.userpass;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import victor.training.spring.web.entity.UserRole;

import java.util.List;

import static org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse;

@Slf4j
@Profile("userpass")
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class UserPassSecurityConfig {
  @PostConstruct
  public void hi() {
    log.warn("Using config");
  }

  @Bean
  @Order(2)
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()); // OK if I only expose REST APIs

//    http.csrf(csrf -> csrf //In case I take <form> post (eg JSP)
//        .csrfTokenRepository(withHttpOnlyFalse())
//        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));

    // adds a http filter that responds to Bro CORS preflight
//    http.cors(Customizer.withDefaults()); // only if .js files come from a CDN (by default CORS requests get blocked)

    http.authorizeHttpRequests(authz -> authz
        //❌EVITA:
//         .requestMatchers(HttpMethod.DELETE, "/api/trainings/*").hasRole("ADMIN")// avoid
        // unless securing a library
            .requestMatchers("/v3/api-docs/**").permitAll()
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
        .username("admin").password("admin").roles("ADMIN").build();
    UserDetails power = User.withDefaultPasswordEncoder()
        .username("power").password("power").roles("POWER").build();
//    return new InMemoryUserDetailsManager(user, admin, power);
    return new InMemoryUserDetailsManager(expandRoles(user), expandRoles(admin), expandRoles(power));
  }

  private UserDetails expandRoles(UserDetails user) {
    log.info("User '{}' has roles {}", user.getUsername(), user.getAuthorities());
    var expendedRoles = user.getAuthorities().stream()
        .map(authority -> authority.getAuthority().substring("ROLE_".length()))
        .flatMap(roleName -> UserRole.expandToSubRoles(List.of(roleName)).stream().map(a->"ROLE_"+a))
        .map(SimpleGrantedAuthority::new)
        .toList();
    log.info("Expanded to sub-roles: " + expendedRoles);
    return User.withUserDetails(user)
        .authorities(expendedRoles)
        .build();
  }

}
