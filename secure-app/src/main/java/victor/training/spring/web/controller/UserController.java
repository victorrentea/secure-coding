package victor.training.spring.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//import victor.training.spring.security.config.keycloak.KeyCloakUtils;
import victor.training.spring.security.keycloak.TokenUtils;
import victor.training.spring.web.controller.dto.CurrentUserDto;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
  @GetMapping("api/user/current")
  public CurrentUserDto getCurrentUser() {
    log.info("Return current user");
    CurrentUserDto dto = new CurrentUserDto();
    // thread local data; returns null if not in an HTTP thread:
      // eg in @KafkaListener, @Scheduled, @Async
    dto.username = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName(); // whatever the authn mechanism used

    dto.authorities = SecurityContextHolder.getContext()
        .getAuthentication()
        .getAuthorities()
        // list of permissions extracted from the token
        .stream()
        .map(Object::toString)
        .toList();
    return dto;
  }
}
