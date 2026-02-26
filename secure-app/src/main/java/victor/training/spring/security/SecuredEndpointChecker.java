package victor.training.spring.security;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Verifies at startup that every HTTP handler method in every {@link RestController}
 * is annotated with a security annotation, either on the method itself or on the class.
 * Fails fast if any unsecured endpoint is found.
 */
@Slf4j
//@Component
@RequiredArgsConstructor
public class SecuredEndpointChecker {

  private static final List<Class<? extends Annotation>> SECURITY_ANNOTATIONS = List.of(
      Secured.class,
      PreAuthorize.class,
      PostAuthorize.class,
      RolesAllowed.class,
      PermitAll.class,
      DenyAll.class
  );

  private static final List<Class<? extends Annotation>> HTTP_MAPPING_ANNOTATIONS = List.of(
      GetMapping.class,
      PostMapping.class,
      PutMapping.class,
      DeleteMapping.class,
      PatchMapping.class,
      RequestMapping.class
  );

  private final ApplicationContext applicationContext;

  @EventListener(ApplicationReadyEvent.class)
  @Order(Ordered.LOWEST_PRECEDENCE) // run after all other listeners
  public void checkAllEndpointsAreSecured() {
    Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);

    List<String> unsecuredEndpoints = new ArrayList<>();

    for (Map.Entry<String, Object> entry : controllers.entrySet()) {
      Class<?> controllerClass = unwrapProxyClass(entry.getValue());
      boolean classIsSecured = hasSecurityAnnotation(controllerClass.getAnnotations());

      for (Method method : controllerClass.getMethods()) {
        if (!isHttpHandlerMethod(method)) {
          continue;
        }
        boolean methodIsSecured = hasSecurityAnnotation(method.getAnnotations());
        if (!classIsSecured && !methodIsSecured) {
          String endpoint = controllerClass.getSimpleName() + "#" + method.getName();
          unsecuredEndpoints.add(endpoint);
          log.error("🔓 Unsecured HTTP endpoint: {}", endpoint);
        }
      }
    }

    if (!unsecuredEndpoints.isEmpty()) {
      throw new IllegalStateException(
          "Application startup aborted: " + unsecuredEndpoints.size() +
          " HTTP endpoint(s) are missing a security annotation (@Secured, @PreAuthorize, @RolesAllowed, @PermitAll, ...):\n  - " +
          String.join("\n  - ", unsecuredEndpoints)
      );
    }

    log.info("✅ All {} REST controller(s) have secured HTTP endpoints.", controllers.size());
  }

  private Class<?> unwrapProxyClass(Object bean) {
    Class<?> clazz = bean.getClass();
    // Spring AOP proxies: unwrap to the target class
    while (clazz.getName().contains("$$") || clazz.getName().contains("CGLIB")) {
      clazz = clazz.getSuperclass();
    }
    return clazz;
  }

  private boolean isHttpHandlerMethod(Method method) {
    for (Class<? extends Annotation> mappingAnnotation : HTTP_MAPPING_ANNOTATIONS) {
      if (method.isAnnotationPresent(mappingAnnotation)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasSecurityAnnotation(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      for (Class<? extends Annotation> securityAnnotation : SECURITY_ANNOTATIONS) {
        if (securityAnnotation.isInstance(annotation)) {
          return true;
        }
      }
    }
    return false;
  }
}

