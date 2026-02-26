package victor.training.spring.web.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDate;

@Slf4j
@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Training {
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private String description;
	private LocalDate startDate;

  @ManyToOne
	private Teacher teacher;

	@Enumerated(EnumType.STRING)
	private ProgrammingLanguage programmingLanguage;

	@CreatedBy // magically extract username from SecurityContext
	private String createdBy;
}
