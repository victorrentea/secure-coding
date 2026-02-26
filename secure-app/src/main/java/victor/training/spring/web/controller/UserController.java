package victor.training.spring.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//import victor.training.spring.security.config.keycloak.KeyCloakUtils;
import victor.training.spring.web.controller.dto.CurrentUserDto;
import victor.training.spring.web.repo.UserRepo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    dto.username = authentication.getName();
    dto.authorities = authentication.getAuthorities().stream()
        .map(Object::toString)
        .toList();
//    dto.managedTeacherIds = userRepo.findByUsername(dto.username).stream()
//        .flatMap(user -> user.getManagedTeacherIds().stream())
//        .toList();

    // fire-and-forget a long-running task
//    Executors.newFixedThreadPool()//NOT ALLOWED IN SPRING❌
//    CompletableFuture.runAsync(() -> other.deep()); // NEVER!
    other.deep();

    List.of(1,2,3,4)
        .parallelStream()
        .forEach(id -> {
//          logic
          // security context lost; REST API call, LAST_MODIFIED_BY, log
        });

//    int sum = 0;
//    for (int i = 0; i < 4; i++) {
//      sum += other.f().join();
//    }
    // start all 4 in parallel
    List<CompletableFuture<Integer>> futures = IntStream.range(0, 4)
        .mapToObj(i -> other.f())
        .toList();
    int sum = futures.stream()
        .map(CompletableFuture::join)
        .reduce(0, Integer::sum);

    return dto;
  }
}

@Service
@Slf4j
class Other {
  @SneakyThrows
  @Async
  public CompletableFuture<Integer> f() {
    Thread.sleep(1000);
    System.out.println("user: " + SecurityContextHolder.getContext().getAuthentication().getName());
    return CompletableFuture.completedFuture(1);
  }

  @Async // 👑 best way to start a background task in Spring
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
