package victor.training.spring.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//import victor.training.spring.security.config.keycloak.KeyCloakUtils;
import victor.training.spring.web.controller.dto.CurrentUserDto;
import victor.training.spring.web.repo.UserRepo;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
  private final UserRepo userRepo;

  @GetMapping("api/user/current")
  public CurrentUserDto getCurrentUser() {
    log.info("Get current user");
    CurrentUserDto dto = new CurrentUserDto();
//    dto.username = "<todo-username>"; // TODO
    dto.username= SecurityContextHolder.getContext().getAuthentication().getName();
//    dto.authorities = List.of(); // TODO
    dto.authorities = SecurityContextHolder.getContext().getAuthentication()
        .getAuthorities()
        .stream()
        .map(Object::toString)
        .toList();
    dto.managedTeacherIds = userRepo.findByUsername(dto.username).stream()
        .flatMap(user -> user.getManagedTeacherIds().stream())
        .toList();
    return dto;
  }
}
//    dto.username = SecurityContextHolder.getContext().getAuthentication().getName();
//    dto.authorities = SecurityContextHolder.getContext().getAuthentication()
//        .getAuthorities()
//        .stream()
//        .map(Objects::toString)
//        .toList();
