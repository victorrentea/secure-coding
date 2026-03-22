package victor.training.web.service;

import org.springframework.stereotype.Component;
import victor.training.web.entity.Teacher;

import java.time.LocalDate;

@Component
public class EmailSender {

    // TODO [SEC] only ADMIN is allowed to send emails. other roles: (a) crash or (b) silently skip
    public void sendScheduleChangedEmail(Teacher teacher, String trainingName, LocalDate newDate) {
        System.out.println("SENDING EMAIL TO TEACHER " + teacher.getName() + " for training " + trainingName + " moved to date " + newDate);

    }
}
