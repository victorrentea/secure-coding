package victor.training.web.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import victor.training.web.entity.Message;

public interface MessageRepo extends JpaRepository<Message, Long> {
}
