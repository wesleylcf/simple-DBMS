import java.nio.ByteBuffer;

public class Block {
  public static final Integer BLOCK_BYTE_SIZE = 200;
  public static final Integer BLOCK_HEADER_BYTE_SIZE = 4;
  private ByteBuffer bytes;

  public Block() {
    bytes = ByteBuffer.allocate(BLOCK_BYTE_SIZE);
    bytes.putInt(0); // block header contains number of records initially 0
  }

  public void insertRecord(Record record) {
    byte[] recordBytes = record.marshal();
    int numRecords = bytes.getInt(0);

    if (recordBytes.length > BLOCK_BYTE_SIZE - BLOCK_HEADER_BYTE_SIZE - numRecords * Record.RECORD_BYTE_SIZE) {
        throw new IllegalArgumentException("Data size exceeds remaining block capacity");
    }

    // Move the position to the end of the block header
    bytes.position(BLOCK_HEADER_BYTE_SIZE + numRecords * Record.RECORD_BYTE_SIZE);

    bytes.put(recordBytes);
    bytes.putInt(0, numRecords + 1);
  }

  public Record readRecord(int recordIndex) {
    int numRecords = bytes.getInt(0);

    // Check if the requested record index is valid
    if (recordIndex < 0 || recordIndex >= numRecords) {
        throw new IllegalArgumentException("Invalid record index");
    }

    int recordPosition = BLOCK_HEADER_BYTE_SIZE + recordIndex * Record.RECORD_BYTE_SIZE;
    bytes.position(recordPosition);

    byte[] recordBytes = new byte[Record.RECORD_BYTE_SIZE];
    bytes.get(recordBytes);

    return Record.unmarshal(recordBytes);
  }
}
