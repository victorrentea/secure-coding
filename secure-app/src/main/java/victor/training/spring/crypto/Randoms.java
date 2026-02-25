package victor.training.spring.crypto;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

import java.util.Random;
import java.util.UUID;

public class Randoms {
  public static void main(String[] args) {
    var r = new Random(); // pseudo random with default seed = clock nanos
//    var r = new Random(1); // for the same seed you get the same sequence of random numbers=>useful for tests
//    var r = new SecureRandom(new byte[]{1}); // uses other entropy sources (OS random, etc)
//    var r = new SecureRandom(); // default seed = clock nanos
    System.out.println("Random: " + r.nextInt(10000) + ", " + r.nextInt(10000) + ", " + r.nextInt(10000));

    var uuid4_1 = UUID.randomUUID();
    var uuid4_2 = UUID.randomUUID();
    System.out.println("UUIDv4: " + (uuid4_1.compareTo(uuid4_2) < 0) + " if " + uuid4_1 + " < " + uuid4_2);

    TimeBasedEpochGenerator uuid7generator = Generators.timeBasedEpochGenerator();
    var uuid7_1 = uuid7generator.generate();
    var uuid7_2 = uuid7generator.generate();
    System.out.println("UUIDv7: " + (uuid7_1.compareTo(uuid7_2) < 0) + " if " + uuid7_1 + " < " + uuid7_2);

    var prev_uuid7 = uuid7_1;
    for (int i = 0; i < 1000000; i++) {
      var new_uuid7 = uuid7generator.generate();
      if (prev_uuid7.compareTo(new_uuid7) >= 0) {
        System.err.println("Not monotonic! " + prev_uuid7 + " >= " + new_uuid7);
        break;
      }
      prev_uuid7 = new_uuid7;
    }
    System.out.println("UUIDv7: All were monotonic!✅");
  }
}
