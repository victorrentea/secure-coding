package victor.training.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//import victor.training.security.config.keycloak.KeyCloakUtils;
import victor.training.security.keycloak.TokenUtils;
import victor.training.web.controller.dto.CurrentUserDto;
import victor.training.web.repo.UserRepo;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
  private final UserRepo userRepo;
  private final Other other;

  @GetMapping("api/user/current")
  public CurrentUserDto getCurrentUser() {
    log.info("Get current user");
    CurrentUserDto dto = new CurrentUserDto();
    dto.username = "<todo-username>"; // TODO X extract username from SecurityContextHolder
    // SOLUTION: dto.username = SecurityContextHolder.getContext().getAuthentication().getName();
    dto.authorities = List.of(); // TODO X extract authorities from SecurityContextHolder
    // SOLUTION: dto.authorities = SecurityContextHolder.getContext().getAuthentication()
    //     .getAuthorities().stream().map(Object::toString).toList();
    dto.managedTeacherIds = List.of(); // TODO X load managed teacher IDs from userRepo
    // SOLUTION: dto.managedTeacherIds = userRepo.findByUsername(dto.username).stream()
    //     .flatMap(user -> user.getManagedTeacherIds().stream()).toList();
    //dto.managedTeacherIds = TokenUtils.getManagedTeacherIds();
    TokenUtils.printTheTokens();
    return dto;
  }
}

@Service
@Slf4j
class Other {
  public void deep() {
    log.info("Somewhere in a repository...");
    String username = "TODO"; // TODO X extract username from SecurityContextHolder
    // SOLUTION: String username = SecurityContextHolder.getContext().getAuthentication().getName();
    log.info("... created_by=" + username);
  }
}
