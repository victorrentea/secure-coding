package victor.training.web.controller.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import victor.training.web.entity.ContractType;
import victor.training.web.entity.TrainingId;

import java.time.LocalDate;

@Configuration
public class RequestUrlPartConverters implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, LocalDate.class, LocalDate::parse);
    }
}
