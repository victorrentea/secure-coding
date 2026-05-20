package victor.training.config;

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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

@Aspect
@Slf4j
@Component
public class RichTextSanitizerAspect {
  @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.RECORD_COMPONENT})
  @Retention(RUNTIME) // survives javac
  public @interface RichText {
  }

  @Around("@within(org.springframework.web.bind.annotation.RestController)")
  public Object sanitizeHttpPayloads(ProceedingJoinPoint pjp) throws Throwable {
    Object[] args = pjp.getArgs();

    Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    for (int i = 0; i < args.length; i++) {
      args[i] = sanitize(args[i], visited);
    }

    Object result = pjp.proceed(args);

    return sanitize(result, Collections.newSetFromMap(new IdentityHashMap<>()));
  }

  // Returns the sanitized value — same reference for mutable objects (mutated in place),
  // or a NEW instance for records (which are immutable, so we rebuild via canonical constructor).
  private static Object sanitize(Object obj, Set<Object> visited) {
    if (obj == null) return null;
    if (isSimpleType(obj.getClass())) return obj;
    if (!visited.add(obj)) return obj; // cycle guard

    Class<?> type = obj.getClass();

    if (type.isRecord()) {
      return sanitizeRecord(obj, visited);
    }

    if (type.isArray()) {
      int length = Array.getLength(obj);
      for (int i = 0; i < length; i++) {
        Object element = Array.get(obj, i);
        Object sanitized = sanitize(element, visited);
        if (sanitized != element) Array.set(obj, i, sanitized);
      }
      return obj;
    }

    if (obj instanceof List<?> list) {
      return sanitizeList(list, visited);
    }

    if (obj instanceof Collection<?> col) {
      for (Object element : col) sanitize(element, visited);
      return obj;
    }

    if (obj instanceof Map<?, ?> map) {
      return sanitizeMap(map, visited);
    }

    while (type != Object.class && type.getPackageName().startsWith("victor.")) {
      for (Field field : type.getDeclaredFields()) {
        field.setAccessible(true);
        Object value;
        try {
          value = field.get(obj);
        } catch (IllegalAccessException e) {
          continue;
        }

        if (field.isAnnotationPresent(RichText.class) && value instanceof String s) {
          String sanitized = sanitizeRichText(s);
          if (!sanitized.equals(s)) trySet(field, obj, sanitized);
        } else {
          Object sanitized = sanitize(value, visited);
          if (sanitized != value) trySet(field, obj, sanitized);
        }
      }
      type = type.getSuperclass();
    }
    return obj;
  }

  private static Object sanitizeRecord(Object record, Set<Object> visited) {
    RecordComponent[] components = record.getClass().getRecordComponents();
    Object[] values = new Object[components.length];
    Class<?>[] paramTypes = new Class<?>[components.length];
    boolean changed = false;

    for (int i = 0; i < components.length; i++) {
      RecordComponent rc = components[i];
      paramTypes[i] = rc.getType();
      Object value;
      try {
        var accessor = rc.getAccessor();
        accessor.setAccessible(true);
        value = accessor.invoke(record);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }

      Object sanitized;
      if (rc.isAnnotationPresent(RichText.class) && value instanceof String s) {
        sanitized = sanitizeRichText(s);
      } else {
        sanitized = sanitize(value, visited);
      }
      values[i] = sanitized;
      if (sanitized != value && (sanitized == null || !sanitized.equals(value))) {
        changed = true;
      }
    }

    if (!changed) return record;

    try {
      Constructor<?> ctor = record.getClass().getDeclaredConstructor(paramTypes);
      ctor.setAccessible(true);
      return ctor.newInstance(values);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to rebuild record " + record.getClass(), e);
    }
  }

  private static Object sanitizeList(List<?> list, Set<Object> visited) {
    boolean changed = false;
    Object[] items = new Object[list.size()];
    for (int i = 0; i < list.size(); i++) {
      Object element = list.get(i);
      Object sanitized = sanitize(element, visited);
      items[i] = sanitized;
      if (sanitized != element) changed = true;
    }
    if (!changed) return list;
    try {
      @SuppressWarnings("unchecked")
      List<Object> mutable = (List<Object>) list;
      for (int i = 0; i < items.length; i++) mutable.set(i, items[i]);
      return list;
    } catch (UnsupportedOperationException immutable) {
      List<Object> copy = new ArrayList<>(items.length);
      Collections.addAll(copy, items);
      return copy;
    }
  }

  private static Object sanitizeMap(Map<?, ?> map, Set<Object> visited) {
    boolean changed = false;
    Map<Object, Object> rebuilt = new LinkedHashMap<>(map.size());
    for (Map.Entry<?, ?> e : map.entrySet()) {
      Object v = e.getValue();
      Object sanitized = sanitize(v, visited);
      rebuilt.put(e.getKey(), sanitized);
      if (sanitized != v) changed = true;
    }
    if (!changed) return map;
    try {
      @SuppressWarnings("unchecked")
      Map<Object, Object> mutable = (Map<Object, Object>) map;
      mutable.putAll(rebuilt);
      return map;
    } catch (UnsupportedOperationException immutable) {
      return rebuilt;
    }
  }

  private static void trySet(Field field, Object obj, Object value) {
    try {
      field.set(obj, value);
    } catch (IllegalAccessException ignored) {
      // final field on a non-record class — give up silently
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
