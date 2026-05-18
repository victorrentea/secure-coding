package victor.training.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;

public class ASymmetricEncryption {

  public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {
    byte[] text = "The Lord of the Rings has been read by many people".getBytes();
    Utils.printText("plain text", text);

    KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
    kpGen.initialize(1024);
    KeyPair keyPair = kpGen.generateKeyPair();
    Utils.printByteArray("private key", keyPair.getPrivate().getEncoded());
    Utils.printByteArray("public key", keyPair.getPublic().getEncoded());

    //encrypt with RSA private key
    byte[] encryptedText = {0};//cipher.doFinal(text);
    Utils.printByteArray("ciphertext", encryptedText);

    //decrypt with RSA public key
    byte[] plainText = {0};
    Utils.printText("decoded text", plainText);
  }
}
