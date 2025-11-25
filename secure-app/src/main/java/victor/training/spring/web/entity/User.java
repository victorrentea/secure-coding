package victor.training.spring.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static java.util.Collections.*;

@Entity
@Table(name = "USERS")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String name;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @ElementCollection
    private Set<Long> managedTeacherIds = new HashSet<>();

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role=%s, managedTeacherIds=%s}", id, username, role, managedTeacherIds);
    }
}
