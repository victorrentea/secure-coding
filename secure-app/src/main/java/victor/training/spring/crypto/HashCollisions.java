package victor.training.spring.crypto;

import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HashCollisions {
  public static void main(String[] args) {
    Set<Data> set = new HashSet<>();
    set.add(new Data("a", "b"));
    set.add(new Data("a", "b"));
    //hashSet.add/contains/remove => O(1) ; ❌❌❌=>O(N)
  }

}
@AllArgsConstructor
class Data {
  private String a,b;

  @Override
  public int hashCode() {
//    return 1; // ❌❌❌
//    return a.hashCode() + b.hashCode();
//    return a.hashCode() + 42*b.hashCode();
    return Objects.hash(a,b);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Data data = (Data) o;
    return Objects.equals(a, data.a) && Objects.equals(b, data.b) ;
  }
}