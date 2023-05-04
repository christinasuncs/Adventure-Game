package zork;

public class Key extends Item {
  private String keyId;
  // test2
  public Key(String keyId, String keyName, int weight) {
    super(weight, keyName, false);
    this.keyId = keyId;
  }

  public String getKeyId() {
    return keyId;
  }
}
