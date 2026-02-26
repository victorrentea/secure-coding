package victor.training.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class GlobalCorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/api/*") nu prinde /api/trainings/2
                registry.addMapping("/api/**")
                        .allowedMethods("*")
                        .allowCredentials(true) // allows receiving session cookie (if using cookies)
//                        .allowedOriginPatterns("http://localhost:8081") // ✅ CDN eg NodeJS
					              .allowedOriginPatterns("http://*") // ❌ Too broad
                ;
                // also don't forget to add .cors() to spring security config to setup http CORS filter
            }
        };
    }
}
