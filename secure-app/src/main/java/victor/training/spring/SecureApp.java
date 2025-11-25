package victor.training.spring;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskDecorator;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import victor.training.spring.web.controller.util.TestDBConnectionInitializer;

import javax.sql.DataSource;
import java.sql.SQLException;

import static java.lang.System.currentTimeMillis;

@SpringBootApplication
@EnableCaching
@Slf4j
@ConfigurationPropertiesScan
@EnableFeignClients
public class SecureApp {
  public static final long t0 = currentTimeMillis();

  public static void main(String[] args) {
    new SpringApplicationBuilder(SecureApp.class)
        .listeners(new TestDBConnectionInitializer())
        .run(args);
  }

  @Autowired
  private Environment environment;

  @Autowired
  private DataSource dataSource;

  @Bean // auto-instrumented by micrometer-tracing
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  public ThreadPoolTaskExecutor poolBar() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("pool-");
    executor.initialize();
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setTaskDecorator(taskDecorator()); // copies TraceID
    Gauge.builder( "pool_pool_size", executor::getPoolSize).register(Metrics.globalRegistry);
    Gauge.builder( "pool_queue_size", executor::getQueueSize).register(Metrics.globalRegistry);
    return executor;
  }

  @Bean // propagate tracing over all Spring-managed thread pools
  public TaskDecorator taskDecorator() {
    return (runnable) -> ContextSnapshot.captureAll().wrap(runnable);
  }

  @Bean // enable propagation of SecurityContextHolder over @Async
  public DelegatingSecurityContextAsyncTaskExecutor taskExecutor(ThreadPoolTaskExecutor poolBar) {
    return new DelegatingSecurityContextAsyncTaskExecutor(poolBar);
  }

  @Bean
  @ConditionalOnMissingBean(RestClient.class)
  public RestClient restClient() {
    return RestClient.create();
  }



  @EventListener(ApplicationReadyEvent.class)
  @Order
  public void printAppStarted() throws SQLException {
    long t1 = currentTimeMillis();
    String jdbcUrl = dataSource.getConnection().getMetaData().getURL();
    log.info("🎈🎈🎈 Application started in {}ms on port :{} connected to DB {}",
        t1 - t0,
        environment.getProperty("local.server.port"),
        jdbcUrl);
  }

}
