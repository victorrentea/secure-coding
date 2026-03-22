package victor.training.web.controller.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import victor.training.config.VisibleForRoleAspect.VisibleForRole;
import victor.training.web.entity.ContractType;
import victor.training.web.entity.ProgrammingLanguage;
import victor.training.web.entity.Training;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class TrainingDto {
	public Long id;
	@Size(min = 3, max = 50)
	public String name;
	public ContractType level;
	public Long teacherId;

	public String teacherBio;
	public ProgrammingLanguage language;
	public String teacherName;
	@JsonFormat(pattern = "dd-MM-yyyy")
	public LocalDate startDate;
  //@RichTextSanitizer.RichText
	public String description;
	@VisibleForRole("ROLE_ADMIN")
	public String createdBy;
	public TrainingDto() {}

	public TrainingDto(Training training) {
		id = training.getId();
		name = training.getName();
		description = training.getDescription();
		startDate = training.getStartDate();
		teacherId = training.getTeacher().getId();
		language = training.getProgrammingLanguage();
		teacherName = training.getTeacher().getName();
		createdBy = training.getCreatedBy();
	}
}
