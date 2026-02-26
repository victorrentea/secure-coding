package victor.training.spring.web.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Entity
@Data // avoid
@EntityListeners(AuditingEntityListener.class)
public class Training {
	public static final int LOCK_DURATION_SECONDS = 20;
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

	@CreatedBy
	private String createdBy;

}
