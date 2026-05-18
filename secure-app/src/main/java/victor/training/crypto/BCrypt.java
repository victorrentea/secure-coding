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
    BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder(STRENGTH, new SecureRandom());
    String encodedPassword = bcryptEncoder.encode("password");

    System.out.println("{bcrypt}" + encodedPassword);

    // TODO assert that bcrypt#matches("password" is true

    // TODO assert that bcrypt#matches("different" is false
  }
}
