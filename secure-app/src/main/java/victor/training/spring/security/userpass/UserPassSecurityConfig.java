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

import java.util.ArrayList;
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

//    http.csrf(csrf -> csrf // in case I take <form> post (eg JSP) + Cookie session
//        .csrfTokenRepository(withHttpOnlyFalse())
//        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));

    // adds a filter that responds to Browser's CORS preflight http request -- by default any CORS request is rejected
    // needed if .js files are loaded from another domain/port: locan NodeJS, cdn.example.com
    http.cors(Customizer.withDefaults());

    http.authorizeHttpRequests(authz -> authz
        // .requestMatchers(HttpMethod.DELETE, "/api/trainings/*").hasRole("ADMIN") // ❌ AVOID: out-of-sync risk
        .anyRequest().authenticated()
    );

    http.formLogin(Customizer.withDefaults()) // display a login page for users
        .userDetailsService(userDetailsService());

//    http.httpBasic(Customizer.withDefaults()) // also accept Authorization: Basic ... request header
//        .userDetailsService(userDetailsService());

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails user = User.withDefaultPasswordEncoder() // ☢️ never use in production
        .username("user").password("user").roles("USER").build();
    UserDetails admin = User.withDefaultPasswordEncoder()
        .username("admin").password("admin").roles("ADMIN").build();
    UserDetails power = User.withDefaultPasswordEncoder()
        .username("power").password("power").roles("POWER").build();
    return new InMemoryUserDetailsManager(user, admin, power);
//    return new InMemoryUserDetailsManager(expandRoles(user), expandRoles(admin), expandRoles(power));
  }

  private UserDetails expandRoles(UserDetails user) {
    log.info("User '{}' has roles {}", user.getUsername(), user.getAuthorities());
    var expendedRoles = user.getAuthorities().stream()
        .map(authority -> authority.getAuthority().substring("ROLE_".length()))
        .flatMap(roleName -> UserRole.expandToSubRoles(List.of(roleName)).stream().map(a->"ROLE_"+a))
        .map(SimpleGrantedAuthority::new)
        .toList();
    var allRoles = new ArrayList<GrantedAuthority>(user.getAuthorities());
    allRoles.addAll(expendedRoles);
    log.info("Expanded to sub-roles: {}", allRoles);
    return User.withUserDetails(user)
        .authorities(allRoles)
        .build();
  }

}
