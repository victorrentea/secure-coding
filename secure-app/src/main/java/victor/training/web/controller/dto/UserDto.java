package victor.training.web.controller.dto;

import victor.training.web.entity.User;
import victor.training.web.entity.UserRole;

public class UserDto {
   public Long id;
   public String name;
   public UserRole profile;

   public UserDto() {
   }

   public UserDto(User user) {
      id = user.getId();
      name = user.getName();
      profile = user.getRole();
   }
}
