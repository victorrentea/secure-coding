package victor.training.spring.crypto;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class Hashing {

  @Test
  public void hashingIntroDemo() throws NoSuchAlgorithmException {
    System.out.println("one way only: cannot infer the original message from the hash");
    hashText("The quick brown fox jumped over the lazy dog.");

    System.out.println("deterministic: same input => same hash");
    // TODO hash the same input and see the same output
    hashText("The quick brown fox jumped over the lazy dog.");

    System.out.println("psuedorandom");
    // TODO insert one typo in the same input, and observe a wildly different hash
    hashText("The quick brown fox jumped ower the lazy dog.");

    System.out.println("fixed length, no matter how large the input");
    // TODO hash an input twice as large, and observe hash has the same size.
    hashText("The quick brown fox jumped ower the lazy dog and a lot more stuff happened after that.");
  }

  private String hashText(String data) throws NoSuchAlgorithmException {
    System.out.println("Input: " + data);

    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    byte[] digest = messageDigest.digest(data.getBytes());

    Utils.printByteArray("Digest", digest);
    return new String(Base64.getEncoder().encode(digest));
  }

  @Test
  void differentContents_differentHash() throws IOException, NoSuchAlgorithmException {
    String file1Hash = hashFile("/file1.xml");
    String file2Hash = hashFile("/file2.xml");
    assertThat(file1Hash).isNotEqualTo(file2Hash);
  }

  @Test
  void sameContents_sameHash() throws IOException, NoSuchAlgorithmException {
    String file1Hash = hashFile("/file1.xml");
    String file2Hash = hashFile("/file1bis.xml");
    assertThat(file1Hash).isEqualTo(file2Hash);
  }

  private static String hashFile(String fileName) throws IOException, NoSuchAlgorithmException {
    try (InputStream inputStream = Hashing.class.getResourceAsStream(fileName)) {
      // TODO MessageDigest.getInstance SHA-256
      byte[] contentBytes = IOUtils.toByteArray(inputStream);

      byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(contentBytes);
      String hashBase64 = Base64.getEncoder().encodeToString(hashBytes);
      System.out.println("hash(" + fileName + ") = " + hashBase64);
      return hashBase64;
    }
  }

  @Test
  void checkFilesAreDifferentTestingTheirHashStream() throws IOException, NoSuchAlgorithmException {
    String file1Hash = hashFileStream("/file1.xml");
    String file2Hash = hashFileStream("/file2.xml");
    assertThat(file1Hash).isNotEqualTo(file2Hash);
  }

  private static String hashFileStream(String fileName) throws IOException, NoSuchAlgorithmException {
    try (InputStream inputStream = Hashing.class.getResourceAsStream(fileName)) {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[8_192];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        digest.update(buffer, 0, bytesRead);
      }

      byte[] hashBytes = digest.digest();
      String hashBase64 = Base64.getEncoder().encodeToString(hashBytes);
      System.out.println("hash(" + fileName + ") = " + hashBase64);
      return hashBase64;
    }
  }

}
