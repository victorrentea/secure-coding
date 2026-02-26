package victor.training.spring.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static victor.training.spring.security.RunAs.withExtraRoles;

@Slf4j
@RequiredArgsConstructor
@Service
public class Job implements CommandLineRunner {
  private final TrainingService trainingService;

//  @EventListener(ApplicationStartedEvent.class)
//  @Scheduled(fixedRate = 2000)
//  @KafkaListener/Rabbit/PubSub message listener // ⭐️⭐️⭐️
  //@RunWithPriviledges({"ROLE_DELETE_TRAINING","ROLE_CREATE_TRAINING"}) // actions  it can do
  public void poll() {
//    trainingService.deleteById(0l);
    withExtraRoles(List.of("ROLE_ADMIN"),
        () -> trainingService.deleteById(0L));
  }

  @Override // batch entry point java .... a b c
  public void run(String... args) throws Exception {

  }
}
