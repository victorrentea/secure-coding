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

  @PostMapping("search") // pragmatic HTTP endpoints
  public List<TrainingDto> search(@RequestBody TrainingSearchCriteria criteria) {
    return trainingService.search(criteria);
  }

  @GetMapping("{id}")
  public TrainingDto get(@PathVariable long id) {
    TrainingDto dto = trainingService.getTrainingById(id);
    return dto;
  }
  @PostMapping
  public void create(@RequestBody @Valid TrainingDto dto) {
    trainingService.createTraining(dto);
  }

  @PutMapping("{trainingId}")
  public void update(@PathVariable Long trainingId, @RequestBody @Valid TrainingDto dto) {
    dto.id = trainingId;
    // TODO use sanitizeRichText(); here + anywhere else?...
    trainingService.updateTraining(dto);
  }

  // TODO 1 Fix UX -> expose GET /user/current/current to return authorities
  // TODO 2 Find
  // TODO 3 Can only delete training if current user.managedTrainingIds includes training.teacher.id
  // TODO 4 [opt] Use Can only delete training if current user.managedTrainingIds includes training.teacher.id
  //  -> use SpEL: @accessController.canDeleteTraining(#id)
  //  -> hasPermission + PermissionEvaluator [GEEK]

  @Secured("ROLE_ADMIN")
//  @Secured("ROLE_CAN_DELETE_TRAINING")
//  @PreAuthorize("@permissionEvaluatorImpl.hasPermission(authentication, #trainingId, 'Training', 'WRITE')")
// @PreAuthorize("hasPermission('TRAINING',#trainigId, 'DELETE')")
  @DeleteMapping("{trainingId}")
  public void delete(@PathVariable Long trainingId) {
    Training training = trainingRepo.findById(trainingId).orElseThrow();
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepo.findByUsernameForLogin(username).orElseThrow();
    if (!user.getManagedTeacherIds().contains(training.getTeacher().getId())) {
      throw new SecurityException("You cannot delete training because you are not a manager for this teacher " + training.getTeacher().getName());
    }

    trainingService.deleteById(trainingId);
  }

  private static String sanitizeRichText(String description) {
    // allows only <b>,<i>... = "whitelisting"
    PolicyFactory sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
    return sanitizer.sanitize(description);
  }
}
