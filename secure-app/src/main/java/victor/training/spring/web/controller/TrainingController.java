package victor.training.spring.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.security.access.annotation.Secured;
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
    dto.description = sanitizeRichText(dto.description);
    return dto;
  }
  @PostMapping
  public void create(@RequestBody @Valid TrainingDto dto) {
    dto.description = sanitizeRichText(dto.description);
    trainingService.createTraining(dto);
  }

  @PutMapping("{trainingId}")
  public void update(@PathVariable Long trainingId, @RequestBody @Valid TrainingDto dto) {
    dto.id = trainingId;
    // hint: sanitizeRichText() TODO undo +others
    dto.description = sanitizeRichText(dto.description);
    trainingService.updateTraining(dto);
  }

  // TODO Fix UX
  // TODO Allow also for 'POWER' role; then remove it. => update UI but forget the BE
  // TODO Allow for authority 'training.delete'
  // TODO Allow only if the current user manages the programming language of the training
  //  (comes as 'admin_for_language' claim in in KeyCloak AccessToken)
  //  -> use SpEL: @accessController.canDeleteTraining(#id)
  //  -> hasPermission + PermissionEvaluator [GEEK]


  // CERINTA: chiar daca ai rolul TRAINING_DELETE,
  // poti sterge cursul doar daca esti admin pe programmingLanguage-ul acelui curs
  // Context: training.programmingLanguage == User.programingLanguage
//  @Secured({"ROLE_ADMIN"/*, "ROLE_POWER"*/})
  @Secured("ROLE_TRAINING_DELETE")
  @DeleteMapping("{trainingId}")
  public void delete(@PathVariable Long trainingId) {
    Training training = trainingRepo.findById(trainingId).orElseThrow();
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepo.findByUsernameForLogin(username).orElseThrow();
    if(user.getAdminForLanguage() != training.getProgrammingLanguage()) {
      throw new SecurityException("You are not allowed to delete this training!");
    }

    trainingService.deleteById(trainingId);
  }

  private static String sanitizeRichText(String description) {
    // allows only <b>,<i>... = "whitelisting"
    PolicyFactory sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
    return sanitizer.sanitize(description);
  }
}
