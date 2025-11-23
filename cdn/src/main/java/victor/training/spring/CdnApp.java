package victor.training.spring;

import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@SpringBootApplication
public class CdnApp implements WebMvcConfigurer {
   public static void main(String[] args) {
       SpringApplication.run(CdnApp.class, args);
   }

   @Override
   public void addResourceHandlers(ResourceHandlerRegistry registry) {
      File staticFolder = new File("secure-app/src/main/resources/static");
      if (!staticFolder.isDirectory()) {
         throw new IllegalArgumentException("Folder to show static files from, does not exist:" + staticFolder.getAbsolutePath());
      }
      registry.addResourceHandler("/**").addResourceLocations(staticFolder.toURI().toString());
   }
   @Override
   public void addViewControllers(ViewControllerRegistry registry) {
      registry.addRedirectViewController("/","/index.html");
   }

   @Bean
   public Filter filter() {
     return (request, response, chain) -> {
       if (response instanceof HttpServletResponse resp) {
         resp.addCookie(new Cookie("APIPATH", "http://localhost:8080"));
       }
       chain.doFilter(request, response);
     };
   }
}