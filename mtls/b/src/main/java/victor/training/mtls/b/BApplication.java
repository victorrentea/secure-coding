package victor.training.mtls.b;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.X509Certificate;

@SpringBootApplication
@RestController
public class BApplication {

  public static void main(String[] args) {
    SpringApplication.run(BApplication.class, args);
  }

  @GetMapping("/hello")
  public String hello(HttpServletRequest request) {
    X509Certificate[] certs = (X509Certificate[])
        request.getAttribute("jakarta.servlet.request.X509Certificate");
    String clientSubject = (certs != null && certs.length > 0)
        ? certs[0].getSubjectX500Principal().getName()
        : "<no client cert>";
    System.out.println("[B] /hello called by client cert: " + clientSubject);
    return "hello from B; you are: " + clientSubject;
  }
}
