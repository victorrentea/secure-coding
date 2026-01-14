package victor.training.spring.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import victor.training.spring.web.controller.dto.TrainingDto;
import victor.training.spring.web.controller.dto.TrainingSearchCriteria;
import victor.training.spring.web.entity.Training;
import victor.training.spring.web.entity.User;
import victor.training.spring.web.repo.TrainingRepo;
import victor.training.spring.web.repo.UserRepo;
import victor.training.spring.web.service.TrainingService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/trainings")
public class TrainingController {
  private final TrainingService trainingService;
  private final UserRepo userRepo;
  private final TrainingRepo trainingRepo;

  @GetMapping
  public List<TrainingDto> getAll() {
    return trainingService.getAllTrainings();
  }

  @PostMapping("search")
  public List<TrainingDto> search(@RequestBody TrainingSearchCriteria criteria) {
    return trainingService.search(criteria);
  }

  @GetMapping("{id}")
  public TrainingDto get(@PathVariable long id) {
    return trainingService.getTrainingById(id);
  }

  @PostMapping
  public void create(@RequestBody @Valid TrainingDto dto) {
    trainingService.createTraining(dto);
  }

  @PutMapping("{trainingId}")
  public void update(@PathVariable Long trainingId, @RequestBody @Valid TrainingDto dto) {
    dto.id = trainingId;
    // TODO sanitizeRichText() against XSS + where else?...
    trainingService.updateTraining(dto);
  }

  // TODO 1 Hide button in UI if not authorized: FE gets roles via /user/current/current to return authorities
  // TODO 2 ROLE_POWER should also be allowed to delete; then remove it
  //  => move to fine-grained role: ROLE_TRAINING_DELETE
  // TODO 3 To delete, current_user.managedTrainingIds must includes training.teacher.id
  //  a) ad-hoc in this method
  //  b) @permissionService.canDeleteTraining(#trainingId) => see PermissionService
  //  c) hasPermission(..) => see PermissionEvaluatorImpl

  @Secured("ROLE_ADMIN")
  @PreAuthorize("hasRole('ROLE_POWER')") // equivalent

// fine-grained role
//  @Secured("ROLE_TRAINING_DELETE")

//  @PreAuthorize("@permissionService.canDeleteTraining(#trainingId)") // b
//  @PreAuthorize("hasPermission(#trainingId, 'TRAINING', 'WRITE')") // c

  @DeleteMapping("{trainingId}")
  public void delete(@PathVariable Long trainingId) {
    // a)
//    Training training = trainingRepo.findById(trainingId).orElseThrow();
//    String username = SecurityContextHolder.getContext().getAuthentication().getName();
//    User user = userRepo.findByUsername(username).orElseThrow();
//    if (!user.getManagedTeacherIds().contains(training.getTeacher().getId())) {
//      throw new SecurityException("You cannot delete training because you are not a manager for this teacher " + training.getTeacher().getName());
//    }

    trainingService.deleteById(trainingId);
  }

  private static String sanitizeRichText(String description) {
    // allows only <b>,<i>... = "whitelisting"
    // also see RichTextSanitizer
    PolicyFactory sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
    return sanitizer.sanitize(description);
  }
}
