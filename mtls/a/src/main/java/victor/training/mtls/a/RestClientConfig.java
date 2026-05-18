package victor.training.mtls.a;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  // No custom SSLContext: RestClient uses java.net.http.HttpClient, which
  // uses SSLContext.getDefault(), which is built from the standard JVM props
  // -Djavax.net.ssl.keyStore / -Djavax.net.ssl.trustStore (see a/pom.xml).
  @Bean
  public RestClient restClient() {
    return RestClient.create("https://localhost:8443");
  }
}
