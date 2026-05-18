package victor.training.crypto;

import java.util.Random;
import java.util.UUID;

public class RandomPlay {
  public static void main(String[] args) {
    
    UUID v4 = UUID.randomUUID();

//    var rand = new Random();
    var rand = new Random(1);
    for (int i = 0; i < 10; i++) {
      System.out.println(rand.nextInt(100));
    }
  }
}
