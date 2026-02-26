package victor.training.spring.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import victor.training.spring.web.entity.Teacher;
import victor.training.spring.web.entity.Training;
import victor.training.spring.web.entity.User;
import victor.training.spring.web.repo.TeacherRepo;
import victor.training.spring.web.repo.TrainingRepo;
import victor.training.spring.web.repo.UserRepo;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("userpass")
public class AuthorizationTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private TrainingRepo trainingRepo;
  @Autowired
  private TeacherRepo teacherRepo;
  @Autowired
  private UserRepo userRepo;

  private Long trainingId;
  private Long teacherId;

  @BeforeEach
  void setUp() {
    Teacher teacher = teacherRepo.save(new Teacher("Mr. Test"));
    teacherId = teacher.getId();

    Training training = new Training();
    training.setName("Test Training");
    training.setTeacher(teacher);
    trainingId = trainingRepo.save(training).getId();
  }

  // ✅ has the role AND manages the teacher => allowed
  @Test
  @WithMockUser(username = "manager", roles = "TRAINING_DELETE")
  void deleteAllowed_whenUserHasRoleAndManagesTeacher() throws Exception {
    userRepo.save(new User()
        .setUsername("manager")
        .setManagedTeacherIds(Set.of(teacherId)));

    mockMvc.perform(delete("/api/trainings/" + trainingId))
        .andExpect(status().isOk());
  }

  // ❌ has the role but does NOT manage the training's teacher => forbidden
  @Test
  @WithMockUser(username = "manager", roles = "TRAINING_DELETE")
  void deleteForbidden_whenUserHasRoleButDoesNotManageTeacher() throws Exception {
    userRepo.save(new User()
        .setUsername("manager")
        .setManagedTeacherIds(Set.of())); // manages nobody

    mockMvc.perform(delete("/api/trainings/" + trainingId))
        .andExpect(status().isForbidden());
  }

  // ❌ manages the teacher but lacks the required role => forbidden
  @Test
  @WithMockUser(username = "manager", roles = "USER")
  void deleteForbidden_whenUserManagesTeacherButLacksRole() throws Exception {
    userRepo.save(new User()
        .setUsername("manager")
        .setManagedTeacherIds(Set.of(teacherId)));

    mockMvc.perform(delete("/api/trainings/" + trainingId))
        .andExpect(status().isForbidden());
  }

  // ❌ unauthenticated => 401
  @Test
  void deleteUnauthorized_whenNotAuthenticated() throws Exception {
    mockMvc.perform(delete("/api/trainings/" + trainingId))
        .andExpect(status().isUnauthorized());
  }
}
