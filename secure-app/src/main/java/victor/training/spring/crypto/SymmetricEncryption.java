package victor.training.spring.crypto;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SymmetricEncryption {

  @Test
  public void testSymmetricEncryption() throws GeneralSecurityException {
      Key key = generateSymmetricKey();
//    Key key = parseSymmetricKeyFromBase64("PAfo78wex8ncPKeixDT3NAcFO/5orNKO");
    Utils.printByteArray("key", key.getEncoded());

    // === get a random Initialization Vector (IV) aka "nonce" = number-used-once
    byte[] iv = generateIv();
    Utils.printByteArray("ivSpec", iv);

    byte[] input = "sensitive data".getBytes();
    Utils.printText("input", input);

    // === encrypt using key + iv
    Cipher encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
    encrypt.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
    byte[] encryptedOutput = encrypt.doFinal(input);
    Utils.printByteArray("encrypted output", encryptedOutput);

    // === decrypt using key + iv
    Cipher decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
    decrypt.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
    byte[] decryptedOutput = decrypt.doFinal(encryptedOutput);
    Utils.printText("decrypted input", decryptedOutput);
  }

  public static byte[] generateIv() throws NoSuchAlgorithmException {
    return generateRandomBytes(16);
  }

  public static Key parseSymmetricKeyFromBase64(String keyBase64) {
    byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
    return new SecretKeySpec(keyBytes, "AES");
  }

  private Key generateSymmetricKey() throws NoSuchAlgorithmException {
    KeyGenerator generator = KeyGenerator.getInstance("AES");
    generator.init(192); // allowed for AES
    return generator.generateKey();
  }

  public static byte[] generateRandomBytes(int count) throws NoSuchAlgorithmException {
    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
    byte[] random = new byte[count];
    secureRandom.nextBytes(random);
    return random;
  }
}
