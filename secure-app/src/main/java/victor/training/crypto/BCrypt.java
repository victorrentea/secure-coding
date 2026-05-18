package victor.training.crypto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

public class BCrypt {
  // bcrypt work factor - increase this when average CPU power of machines increases
  public static final int STRENGTH = 10;

  // TO increase power, re-encode it at first user login (when you have the cleartext password); reset user's pass

  @Test
  void explore() {
    //at user create account / when you set a password for your /actuator
//    BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder(STRENGTH, new SecureRandom());
//    String encodedPassword = bcryptEncoder.encode("password");

    String encodedPassword = "$2a$10$aLnAE79inlS22L.ZvDo6lu.Jx693Nd3LQy0CtArgVlFm8QFy2AOh6";
    System.out.println("{bcrypt}" + encodedPassword);

    System.out.println(new BCryptPasswordEncoder(STRENGTH, new SecureRandom())
        .matches("password", encodedPassword));

    // TODO assert that bcrypt#matches("different" is false
    System.out.println(new BCryptPasswordEncoder(STRENGTH, new SecureRandom())
        .matches("wrong", encodedPassword));
  }
}
