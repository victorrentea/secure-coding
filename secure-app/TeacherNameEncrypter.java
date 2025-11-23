package victor.training.spring.web.entity;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import victor.training.spring.crypto.SymmetricEncryption;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.util.Base64;

@Converter
@Slf4j
public class TeacherNameEncrypter implements AttributeConverter<String, String> {
  private Key key;

  @Value("${symmetric.key.base64}")
  public void setKey(String keyBase64) {
    log.info("Using symmetric key: {}", keyBase64);
    key = SymmetricEncryption.parseSymmetricKeyFromBase64(keyBase64);
  }

  @Override
  @SneakyThrows
  public String convertToDatabaseColumn(String attribute) {
//    return Base64.getEncoder().encodeToString(attribute.getBytes());

    Cipher encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
    var iv = SymmetricEncryption.generateIv();
    encrypt.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
    byte[] encryptedOutput = encrypt.doFinal(attribute.getBytes());

    return Base64.getEncoder().encodeToString(encryptedOutput) +
           "." +
           Base64.getEncoder().encodeToString(iv);
  }

  @Override
  @SneakyThrows
  public String convertToEntityAttribute(String dbData) {
//    return new String(Base64.getDecoder().decode(dbData));

    String[] parts = dbData.split("\\.");
    byte[] encryptedData = Base64.getDecoder().decode(parts[0]);
    byte[] iv = Base64.getDecoder().decode(parts[1]);

    Cipher decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
    decrypt.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
    byte[] decryptedOutput = decrypt.doFinal(encryptedData);
    return new String(decryptedOutput);
  }
}
