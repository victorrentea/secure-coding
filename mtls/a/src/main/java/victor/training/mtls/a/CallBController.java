package victor.training.mtls.a;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class CallBController {

  private final RestClient restClient;

  public CallBController(RestClient restClient) {
    this.restClient = restClient;
  }

  @GetMapping("/call-b")
  public String callB() {
    String body = restClient.get().uri("/hello").retrieve().body(String.class);
    String result = "B responded — body: " + body;
    System.out.println("[A] " + result);
    return result;
  }
}
