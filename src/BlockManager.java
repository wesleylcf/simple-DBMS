import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BlockManager {
    private int tombstones = 0;
    private Map<Integer, ArrayList<Integer>> tombstonesByBlock = new HashMap<>();
    private Integer numRecords;
    private ArrayList<Block> blocks = new ArrayList<>();

    public BlockManager(List<Record> seedRecords) {
        numRecords = seedRecords.size();
        blocks.add(new Block());

        for (Record r : seedRecords) {
            insertRecord(r);
        }
    }

    /**
     * We do an append only log which is performant as it makes use of sequential writes.
     * @param record Record to be inserted
     */
    public void insertRecord(Record r) {
      if (blocks.get(blocks.size()-1).isFull()) {
        blocks.add(new Block());
      }
      Block finalBlock = blocks.get(blocks.size()-1);
      finalBlock.insertRecord(r);
      numRecords ++;
      // Update b tree with a reference to the record (block #, record #)
    }

    /**
     * Instead of deleting on shift, mark the record with RecordTombstone and batch delete and shift when tombstone exceed 20%
     * @param record Record to delete
     */
    public void deleteRecord(Record r) {
      // Get (block #, record #) from default B tree;
      // RecordTombstone tombstone = block.removeRecord(r.getUuid());
      // ArrayList<Integer> blockTombstones = tombstones.getOrDefault(blockIndex, new ArrayList<>());
      // blockTombstones.add(recordIndex);
      // tombstonesByBlock.put(blockIndex, blockTombstones);
      tombstones ++;
      if (tombstones > Math.round(numRecords * 0.2)) {
        runCompaction();
      }
    }

    public void readRecordByPrimaryKey() {
      // Get (block #, record #) from default B tree
      // If valid block # and record # do constant time lookup. Otherwise do linear lookup.
    }

    public void updateRecordByPrimaryKey() {
      // Call readRecordByPrimaryKey
      // Call deleteRecord
      // Call insertRecord with the updated properties
    }

    /**
     * Delete tombstones and reclaim space to reduce fragmentation. Tombstones are a result of updates or deletions.
     */
    private void runCompaction() {
      int currentBlockIndex = 0;  // Current block index
      int currentIndexInBlock = 0;  // Index in the current block
      int numBlocks = blocks.size();
  
      // Iterate through blocks and process tombstones
      for (int i = 0; i < numBlocks; i++) {
          Block currentBlock = blocks.get(i);
          int numRecords = currentBlock.getRecordCount();
  
          // Iterate through records in the current block
          for (int j = 0; j < numRecords; j++) {
              Record record = currentBlock.getRecordAt(j);
              if (!(record.isTombstone())) {
                  // Move valid records to the current block and index in block
                  blocks.get(currentBlockIndex).insertRecordAt(currentIndexInBlock, record);
                  currentIndexInBlock++;
  
                  // If the current block is full, move to the next block
                  if (blocks.get(currentBlockIndex).isFull()) {
                      currentBlockIndex++;
                      currentIndexInBlock = 0;
                  }
              } else {
                  tombstones--;
              }
          }
      }
  
      // Remove dangling blocks
      blocks.subList(currentBlockIndex + 1, blocks.size()).clear();
  
      // Reset tombstones and update the B tree index
      tombstonesByBlock.clear();
      numRecords -= tombstones;
  }
  
  
  

    public void printState() {
      System.out.println("#####\tPrinting state of BlockManager\t#####");
      System.out.println(String.format("Number of records: %d", numRecords));
      System.out.println(String.format("Size of record in bytes: %d", Record.RECORD_BYTE_SIZE));
      System.out.println(String.format("Max number of records in a block: %d", Block.MAX_RECORDS_PER_BLOCK));
      System.out.println(String.format("Current number of blocks(before batch deletion): %d", blocks.size()));
      System.out.println();
    }

}