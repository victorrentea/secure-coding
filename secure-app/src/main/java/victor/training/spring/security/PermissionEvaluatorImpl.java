package victor.training.spring.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import victor.training.spring.web.entity.User;
import victor.training.spring.web.repo.TrainingRepo;
import victor.training.spring.web.repo.UserRepo;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Component
public class PermissionEvaluatorImpl implements PermissionEvaluator {
   private final TrainingRepo trainingRepo;

   @Configuration
   public static class Config {
      @Bean
      MethodSecurityExpressionHandler methodSecurityExpressionHandler(PermissionEvaluatorImpl evaluator) {
         var h = new DefaultMethodSecurityExpressionHandler();
         h.setPermissionEvaluator(evaluator);
         return h;
      }
   }


   private enum PermissionType {
      WRITE, READ
   }
   @Override
   public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
      log.debug("Checking permission '{}' for user '{}' on object '{}'", permission, auth.getName(), targetDomainObject);
      if ((auth == null) || (targetDomainObject == null) || !(permission instanceof String)) {
         return false;
      }
      String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();

      return hasPrivilege(auth, targetType,
          PermissionType.valueOf(permission.toString().toUpperCase()),
          null);
   }

   @Override
   public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
      log.debug("Checking permission '{}' for user '{}' on object '{}' id:{}",
          permission, auth.getName(), targetType, targetId);
      if ((auth == null) || (targetType == null) || !(permission instanceof String)) {
         return false;
      }
      return hasPrivilege(auth, targetType.toUpperCase(),
          PermissionType.valueOf(permission.toString().toUpperCase()),
          targetId);
   }

   private boolean hasPrivilege(Authentication authentication, String targetType, PermissionType permission, Serializable targetId) {
      switch (targetType) {
         case "TRAINING":
            return hasTrainingPrivilege(authentication, permission, (Long) targetId);
         default:
            throw new IllegalArgumentException("Unknown type: " + targetType);
      }
   }

   private boolean hasTrainingPrivilege(Authentication authentication, PermissionType permission, Long trainingId) {
      if (permission == PermissionType.READ) {
         return true;
      }
      Set<Long> teacherIds = getManagedTeacherIdsFromUsersTable(authentication);
//      Set<Long> teacherIds = getManagedTeacherIdsFromAccessToken(authentication);
      log.info("Current user manages teacher IDs = {}", teacherIds);
      Long teacherId = trainingRepo.findById(trainingId).get().getTeacher().getId();
      log.info("Training.teacher.id = {}", teacherId);
      return teacherIds.contains(teacherId);
   }

   private Set<Long> getManagedTeacherIdsFromAccessToken(Authentication authentication) {
//      KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal = (KeycloakPrincipal<KeycloakSecurityContext>) authentication.getPrincipal();
//      keycloakPrincipal.getKeycloakSecurityContext().getToken()
//              .getOtherClaims()
      return Collections.emptySet();
   }

   private final UserRepo userRepo;

   private Set<Long> getManagedTeacherIdsFromUsersTable(Authentication authentication) {
      String username = authentication.getName();
      User user = userRepo.findByUsername(username)
              .orElseThrow(()->new IllegalArgumentException("User '" + username + "' not found in USERS table."));
      return user.getManagedTeacherIds();
      // When using token-based authorization, the user from DB is typically cached in the user session
   }
}
