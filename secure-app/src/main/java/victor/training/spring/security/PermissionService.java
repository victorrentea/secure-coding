package victor.training.spring.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import victor.training.spring.web.entity.Training;
import victor.training.spring.web.entity.User;
import victor.training.spring.web.repo.TrainingRepo;
import victor.training.spring.web.repo.UserRepo;

import java.util.Set;

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
    Set<Long> managedTeacherIds = user.getManagedTeacherIds(); // or from JWT token
    return managedTeacherIds.contains(training.getTeacher().getId());
  }
}
