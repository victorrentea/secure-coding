package victor.training.web.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import victor.training.web.entity.ContractType;
import victor.training.web.entity.Teacher;

import java.util.List;

public interface TeacherRepo extends JpaRepository<Teacher, Long> {
   List<Teacher> findByContractType(ContractType contractType);
}
