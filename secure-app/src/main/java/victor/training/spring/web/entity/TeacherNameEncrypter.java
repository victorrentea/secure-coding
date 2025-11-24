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
    // TODO generate iv
    // TODO encrypt attribute.getBytes() using key + iv
    // TODO save in db encrypted base64(bytes) + "." + base64(iv)
    return Base64.getEncoder().encodeToString(attribute.getBytes());
  }

  @Override
  @SneakyThrows
  public String convertToEntityAttribute(String dbData) {
    // TODO parse > decrypt
    return new String(Base64.getDecoder().decode(dbData));
  }
}
