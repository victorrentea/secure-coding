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
//    dto.username = "<todo-username>"; // TODO
//    dto.authorities = List.of(); // TODO
    dto.username = SecurityContextHolder.getContext().getAuthentication().getName();
    dto.authorities = SecurityContextHolder.getContext().getAuthentication()
        .getAuthorities()
        .stream()
        .map(Objects::toString)
        .toList();
    return dto;
  }
}
