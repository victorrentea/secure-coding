package victor.training.spring.crypto;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RandomNot {
  public static void main(String[] args) {
    // pseudo-random, based on an intial seed
    Random r = new Random(1); // if you fix the seed, you get the same sequence of random numbers
//    Random r = new Random();
    System.out.println(r.nextInt(10000));
    System.out.println(r.nextInt(10000));
    System.out.println(r.nextInt(10000));

    // you can use a fixed seed to generate 'random' test data, that is the same across test runs
  }
}
