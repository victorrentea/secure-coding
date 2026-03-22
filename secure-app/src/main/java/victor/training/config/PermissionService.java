package victor.training.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import victor.training.web.entity.Training;
import victor.training.web.entity.User;
import victor.training.web.repo.TrainingRepo;
import victor.training.web.repo.UserRepo;

@Slf4j
@RequiredArgsConstructor
@Service
public class PermissionService {
  private final TrainingRepo trainingRepo;
  private final UserRepo userRepo;

  public boolean canDeleteTraining(long trainingId) {
    Training training = trainingRepo.findById(trainingId).orElseThrow();
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepo.findByUsername(username).orElseThrow();
    return user.getManagedTeacherIds().contains(training.getTeacher().getId());
  }
}
