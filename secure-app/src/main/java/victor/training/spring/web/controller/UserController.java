package victor.training.spring.web.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//import victor.training.spring.security.config.keycloak.KeyCloakUtils;
import victor.training.spring.web.controller.dto.CurrentUserDto;
import victor.training.spring.web.repo.UserRepo;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
  private final UserRepo userRepo;
  private final Other other;
//  static ThreadLocal<String> tl = new ThreadLocal<>();

  @GetMapping("api/user/current")
  public CurrentUserDto getCurrentUser() {
    log.info("Get current user");
    CurrentUserDto dto = new CurrentUserDto();

    dto.username = SecurityContextHolder.getContext().getAuthentication().getName();
//    dto.authorities = List.of(); // TODO
//    dto.managedTeacherIds = userRepo.findByUsername(dto.username).stream()
//        .flatMap(user -> user.getManagedTeacherIds().stream())
//        .toList();
    CompletableFuture.runAsync(() -> other.deep()); // fire-and-forget a long-running task
    return dto;
  }
}
//

@Service
@Slf4j
class Other {
  public void deep() {
    try {
      String username = SecurityContextHolder.getContext().getAuthentication().getName();
      log.info("Somewhere in a repository... created_by=" + username);
    } catch (Exception e) {
      log.error("ERROR", e);
    }
  }
}
//    dto.username = SecurityContextHolder.getContext().getAuthentication().getName();
//    dto.authorities = SecurityContextHolder.getContext().getAuthentication()
//        .getAuthorities()
//        .stream()
//        .map(Objects::toString)
//        .toList();
