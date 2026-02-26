package victor.training.spring.web.controller;

import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UnsecuredController {

  @PermitAll
  @GetMapping("unsecured/welcome")
  @CrossOrigin(originPatterns = "*")
  public String showWelcomeInfo() {
    return "In case of problems call Santa: 0800SANTACLAUS";
  }
}
