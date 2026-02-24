package victor.training.spring.crypto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

public class BCrypt { // the only sane way to store password

  // bcrypt work factor - increase this when average CPU power of machines increases
  public static final int STRENGTH = 10;

  // TO increase power, re-encode it at first user login (when you have the cleartext password); reset user's pass

  @Test
  void explore() {
    BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder(STRENGTH, new SecureRandom());
    String encodedPassword = bcryptEncoder.encode("password");

    System.out.println("YOu store in DB/.property this: {bcrypt}" + encodedPassword);

    // later on when a user:pass combination has to be checked, you do:
    // TODO assert that bcrypt#matches is true
    BCryptPasswordEncoder bcryptMatcher = new BCryptPasswordEncoder(STRENGTH, new SecureRandom());
    Assertions.assertThat(bcryptMatcher.matches("password", encodedPassword)).isTrue();

    // TODO assert that bcrypt#matches is false
    Assertions.assertThat(bcryptMatcher.matches("different", encodedPassword)).isFalse();
  }


  //


}
