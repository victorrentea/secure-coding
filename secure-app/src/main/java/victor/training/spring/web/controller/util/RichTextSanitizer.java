package victor.training.spring.web.controller.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

@Aspect
@Slf4j
@Component
public class RichTextSanitizer {
  @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
  @Retention(RUNTIME) // survives javac
  public @interface RichText {
  }

  @Around("@within(org.springframework.web.bind.annotation.RestController)")
  public Object sanitizeHttpPayloads(ProceedingJoinPoint pjp) throws Throwable {
    Object[] args = pjp.getArgs();

    for (Object arg : args) {
      sanitizeObjectGraph(arg);
    }

    Object result = pjp.proceed(args);

    sanitizeObjectGraph(result);

    return result;
  }

  private static void sanitizeObjectGraph(Object root) {
    if (root == null) return;
    Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    sanitizeRecursive(root, visited);
  }

  private static void sanitizeRecursive(Object obj, Set<Object> visited) {
    if (obj == null) return;

    if (!visited.add(obj)) {
      return; // avoid cycles
    }

    if (isSimpleType(obj.getClass())) {
      return;
    }

    if (obj.getClass().isArray()) {
      int length = Array.getLength(obj);
      for (int i = 0; i < length; i++) {
        Object element = Array.get(obj, i);
        sanitizeRecursive(element, visited);
      }
      return;
    }

    if (obj instanceof Collection) {
      for (Object element : (Collection<?>) obj) {
        sanitizeRecursive(element, visited);
      }
      return;
    }

    if (obj instanceof Map) {
      for (Object value : ((Map<?, ?>) obj).values()) {
        sanitizeRecursive(value, visited);
      }
      return;
    }

    Class<?> type = obj.getClass();
    while (type != Object.class && type.getPackageName().startsWith("victor.")) {
      Field[] fields = type.getDeclaredFields();
      for (Field field : fields) {
        field.setAccessible(true);
        Object value;
        try {
          value = field.get(obj);
        } catch (IllegalAccessException e) {
          continue;
        }

        if (field.isAnnotationPresent(RichText.class) && value instanceof String) {
          try {
            field.set(obj, sanitizeRichText((String) value));
          } catch (IllegalAccessException ignored) {
          }
        } else {
          sanitizeRecursive(value, visited);
        }
      }
      type = type.getSuperclass();
    }
  }

  private static boolean isSimpleType(Class<?> type) {
    return type.isPrimitive()
        || type.equals(String.class)
        || Number.class.isAssignableFrom(type)
        || Boolean.class.equals(type)
        || Character.class.equals(type)
        || Enum.class.isAssignableFrom(type)
        || Class.class.equals(type);
  }


  private static String sanitizeRichText(String originalString) {
    // allows only <b>,<i>... = "whitelisting"
    PolicyFactory sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
    String sanitizedString = sanitizer.sanitize(originalString);
    if (!sanitizedString.equals(originalString)) {
      log.error("Sanitized {} -> {}", originalString, sanitizedString);
    }
    return sanitizedString;
  }
}
