package victor.training.spring.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static victor.training.spring.security.RunAs.withExtraRoles;

@Slf4j
@RequiredArgsConstructor
@Service
public class Job {
  private final TrainingService trainingService;

//  @Scheduled(fixedRate = 2000)
  public void poll() {
    withExtraRoles(List.of("ROLE_ADMIN"),
        () -> trainingService.deleteById(0L));
  }
}
