package victor.training.spring.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RunAs {

  private RunAs() {}

  public static void withExtraRoles(List<String> extraRoles, Runnable action) {
    withExtraRoles(extraRoles, () -> {
      action.run();
      return null;
    });
  }

  public static <T> T withExtraRoles(List<String> extraRoles, Supplier<T> action) {
    SecurityContext originalContext = SecurityContextHolder.getContext();
    try {
      Authentication original = originalContext.getAuthentication();
      List<SimpleGrantedAuthority> elevated = new ArrayList<>();
      if (original != null) {
        original.getAuthorities().stream()
            .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
            .forEach(elevated::add);
      }
      extraRoles.stream().map(SimpleGrantedAuthority::new).forEach(elevated::add);

      Authentication elevated_auth = new UsernamePasswordAuthenticationToken(
          original != null ? original.getPrincipal() : "system",
          original != null ? original.getCredentials() : null,
          elevated
      );

      SecurityContext elevatedContext = SecurityContextHolder.createEmptyContext();
      elevatedContext.setAuthentication(elevated_auth);
      SecurityContextHolder.setContext(elevatedContext); // ☢️☢️☢️☢️☢️☢️

      return action.get();
    } finally {
      SecurityContextHolder.setContext(originalContext); // #musthave !
    }
  }
}
