import java.nio.ByteBuffer;

public class Record {
  public static final Integer RECORD_BYTE_SIZE = 20;
  public static final Integer UUID_BYTE_SIZE = 10;
  public static final Integer RATING_BYTE_SIZE = 4;
  public static final Integer VOTES_BYTE_SIZE = 4;

  private String uuid;
  private Float averageRating;
  private Integer numVotes;
  
  private Byte padding1;
  private Byte padding2;

  public Record(String tconst, Float averageRating, Integer numVotes) {
    this.uuid = tconst;
    this.averageRating = averageRating;
    this.numVotes = numVotes;
  }

  /**
   * Returns Byte[] of the current record
   */
  public byte[] marshal() {
    ByteBuffer buffer = ByteBuffer.allocate(RECORD_BYTE_SIZE); // Total size including padding bytes

    byte[] uuidBytes = new byte[UUID_BYTE_SIZE];
    uuidBytes = uuid.getBytes();
    buffer.put(uuidBytes);

    buffer.put(padding1);
    buffer.put(padding2);

    // Put float and int values into the buffer
    buffer.putFloat(averageRating);
    buffer.putInt(numVotes);

    return buffer.array();
}

/**
 * Converts a Byte[] back to a Record
 */
public static Record unmarshal(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);

    byte[] uuidBytes = new byte[UUID_BYTE_SIZE + 2]; // include 2 bytes of padding
    buffer.get(uuidBytes);
    String uuid = new String(uuidBytes);

    float averageRating = buffer.getFloat();
    int numVotes = buffer.getInt();

    return new Record(uuid, averageRating, numVotes);
  }

  @Override
  public String toString() {
      return String.format("{ uuid:'%s', averageRating:%f, numVotes:%d }", this.uuid, this.averageRating, this.numVotes);
  }

  public String getUuid() {
    return uuid;
  }

  public Float getAverageRating() {
    return averageRating;
  }

  public Integer getNumVotes() {
    return numVotes;
  }
}