import java.util.ArrayList;
import java.util.HashSet;

/**
 * Blocks are logical units representing a particular slice of the Disk
 * This class is to encapsulate the logic for interactions between Disk and Block
 */
class StorageManager {
    private Integer numRecords = 0;
    private int occupiedBlocks = 0;
    Disk disk;
    StorageConfiguration config;
    CrashRecovery recovery;
    BPlusTree bPlusTree;

    public StorageManager(Disk disk, StorageConfiguration storageConfiguration, CrashRecovery recovery, BPlusTree bPlusTree) {
        this.disk = disk;
        this.config = storageConfiguration;
        this.recovery = recovery;
        this.bPlusTree = bPlusTree;
    }

    /**
     * We do an append only log which is performant as it makes use of sequential writes.
     *
     * @param record Record to be inserted
     */
    public void insertRecord(Record r) {
        recovery.logOperation(CrashRecovery.OPERATION.INSERT, r);
        int blockNumber;
        int recordOffsetInBlock;

        if (occupiedBlocks == 0) {
            Block block = new Block();
            occupiedBlocks++;
            recordOffsetInBlock = 0; // New block, so the offset is 0
            block.insertRecord(r);
            disk.writeBlock(1, block);
            blockNumber = occupiedBlocks;
        } else {
            Block block = disk.getBlock(occupiedBlocks);
            if (block.isFull()) {
                block = new Block();
                occupiedBlocks++;
                recordOffsetInBlock = 0; // New block, so the offset is 0
                block.insertRecord(r);
                disk.writeBlock(occupiedBlocks, block);
                blockNumber = occupiedBlocks;
            } else {
                recordOffsetInBlock = block.getRecordCount();
                blockNumber = occupiedBlocks;
                block.insertRecord(r);
                disk.writeBlock(occupiedBlocks, block);
            }
        }
        numRecords++;
        this.checkAndRunCompaction();

        // Update b tree
        Address address = new Address(blockNumber, recordOffsetInBlock);
        bPlusTree.insert(r.getNumVotes(), address);
    }

    /**
     * Instead of deleting on shift, mark the record with RecordTombstone and batch delete and shift when tombstone exceed 20%
     *
     * @param record Record to delete
     */
    public void deleteRecord(Record r) {
        recovery.logOperation(CrashRecovery.OPERATION.DELETE, r);
        Record recordTombstone = new Record(r.getUuid(), r.getAverageRating(), r.getNumVotes(), (short) 1);
        insertRecord(recordTombstone);
        bPlusTree.deleteKey(r.getNumVotes());
        this.checkAndRunCompaction();
    }

    /**
     * Obtain the list of addresses from the B+ tree based on numVotes
     *
     * @param numVotes The number of votes to search for in the records
     */
    public void retrieveRecordsByNumVotes(int numVotes) {
        long startTime = System.currentTimeMillis();

        ArrayList<Address> addresses = bPlusTree.getRecordsWithKey(numVotes);
        HashSet<Integer> accessedBlocks = new HashSet<>();
        int blockAccessCounter = 0;
        double averageRatingSum = 0;
        int recordCounter = 0;

        for (Address address : addresses) {
            // Use the block ID directly from the address
            int blockId = address.returnId();
            // Check if the block has been accessed before
            if (accessedBlocks.add(blockId)) {
                blockAccessCounter++;
            }

            // Retrieve the block and the specific record within the block
            Block block = disk.getBlock(blockId);
            Record recordObtained = block.getRecordAt(address.getOffset());

            if (!recordObtained.isTombstone()) {
                averageRatingSum += recordObtained.getAverageRating();
                recordCounter++;
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        double averageRating = recordCounter > 0 ? averageRatingSum / recordCounter : 0;

        printStatistics("B+ Tree", blockAccessCounter, averageRating, duration);
    }

    /**
     * Obtain the list of addresses from the disk by brute-force linear scan
     *
     * @param numVotes The number of votes to search for in the records
     */
    public void linearScanByNumVotes(int numVotes) {
        long startTime = System.currentTimeMillis();

        HashSet<Integer> accessedBlocks = new HashSet<>();
        int blockAccessCounter = 0;
        double averageRatingSum = 0;
        int recordCounter = 0;

        for (int blockId = 1; blockId <= occupiedBlocks; blockId++) {
            Block block = disk.getBlock(blockId);
            if (accessedBlocks.add(blockId)) {
                blockAccessCounter++;
            }
            for (int recordIndex = 0; recordIndex < block.getRecordCount(); recordIndex++) {
                Record recordObtained = block.getRecordAt(recordIndex);
                if (!recordObtained.isTombstone() && recordObtained.getNumVotes() == numVotes) {
                    averageRatingSum += recordObtained.getAverageRating();
                    recordCounter++;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        double averageRating = recordCounter > 0 ? averageRatingSum / recordCounter : 0;

        printStatistics("Brute-force Linear Scan", blockAccessCounter, averageRating, duration);
    }

    /**
     * Obtain the list of addresses from the B+ tree based on range of numVotes
     *
     * @param min The minimum numVotes value in the range
     * @param max The maximum numVotes value in the range
     */
    public void retrieveRecordsByNumVotesRange(int min, int max) {
        long startTime = System.currentTimeMillis();

        ArrayList<Address> addresses = bPlusTree.getRecordsWithKeyInRange(min, max);
        HashSet<Integer> accessedBlocks = new HashSet<>();
        int blockAccessCounter = 0;
        double averageRatingSum = 0;
        int recordCounter = 0;

        for (Address address : addresses) {
            // Use the block ID directly from the address
            int blockId = address.returnId();
            // Check if the block has been accessed before
            if (accessedBlocks.add(blockId)) {
                blockAccessCounter++;
            }

            // Retrieve the block and the specific record within the block
            Block block = disk.getBlock(blockId);
            Record recordObtained = block.getRecordAt(address.getOffset());

            if (!recordObtained.isTombstone()) {
                averageRatingSum += recordObtained.getAverageRating();
                recordCounter++;
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        double averageRating = recordCounter > 0 ? averageRatingSum / recordCounter : 0;

        printStatistics("B+ Tree", blockAccessCounter, averageRating, duration);
    }

    /**
     * Obtain the list of addresses from the disk by brute-force linear scan
     *
     * @param min The minimum numVotes value in the range
     * @param max The maximum numVotes value in the range
     */
    public void linearScanByNumVotesRange(int min, int max) {
        long startTime = System.currentTimeMillis();

        HashSet<Integer> accessedBlocks = new HashSet<>();
        int blockAccessCounter = 0;
        double averageRatingSum = 0;
        int recordCounter = 0;

        for (int blockId = 1; blockId <= occupiedBlocks; blockId++) {
            Block block = disk.getBlock(blockId);
            if (accessedBlocks.add(blockId)) {
                blockAccessCounter++;
            }

            for (int recordIndex = 0; recordIndex < block.getRecordCount(); recordIndex++) {
                Record recordObtained = block.getRecordAt(recordIndex);
                if (!recordObtained.isTombstone() && recordObtained.getNumVotes() >= min && recordObtained.getNumVotes() <= max) {
                    averageRatingSum += recordObtained.getAverageRating();
                    recordCounter++;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        double averageRating = recordCounter > 0 ? averageRatingSum / recordCounter : 0;

        printStatistics("Brute-force Linear Scan", blockAccessCounter, averageRating, duration);
    }

    /**
     * Obtain the list of addresses from the B+ tree based on range of numVotes
     *
     * @param numVotes The number of votes to search for in the records
     */
    public void deleteRecordsByNumVotes(int numVotes) {
        long startTime = System.currentTimeMillis();

        ArrayList<Address> addresses = bPlusTree.getRecordsWithKey(numVotes);
        int blockAccessCounter = 0;
        HashSet<Integer> accessedBlocks = new HashSet<>();

        for (Address address : addresses) {
            int blockId = address.returnId();
            if (accessedBlocks.add(blockId)) {
                blockAccessCounter++;
            }

            Block block = disk.getBlock(blockId);
            Record recordToDelete = block.getRecordAt(address.getOffset());
            if (!recordToDelete.isTombstone() && recordToDelete.getNumVotes() == numVotes) {
                deleteRecord(recordToDelete);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        printStatistics("B+ Tree", blockAccessCounter, null, duration);
    }

    /**
     * Obtain the list of addresses from the disk by brute-force linear scan
     *
     * @param numVotes The number of votes to search for in the records.
     *
     */
    public void linearScanDeleteByNumVotes(int numVotes) {
        long startTime = System.currentTimeMillis();
        int blockAccessCounter = 0;
        HashSet<Integer> accessedBlocks = new HashSet<>();

        for (int blockId = 1; blockId <= occupiedBlocks; blockId++) {
            Block block = disk.getBlock(blockId);
            if (accessedBlocks.add(blockId)) {
                blockAccessCounter++;
            }

            for (int recordIndex = 0; recordIndex < block.getRecordCount(); recordIndex++) {
                Record recordToDelete = block.getRecordAt(recordIndex);
                if (!recordToDelete.isTombstone() && recordToDelete.getNumVotes() == numVotes) {
                    deleteRecord(recordToDelete);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        printStatistics("Brute-force Linear Scan", blockAccessCounter, null, duration);
    }

//    public void updateRecordByPrimaryKey() {
//        // Call readRecordByPrimaryKey
//        // Call deleteRecord
//        // Call insertRecord with the updated properties
//    }

    /**
     * For simplicity assume all blocks are full
     */
    public float getDiskUtilization () {
        return (float) (occupiedBlocks * Block.BLOCK_BYTE_SIZE) / Disk.DISK_BYTE_SIZE;
    }

    /**
     * Check if disk utilization is over 90%.
     * If so delete tombstones and reclaim space to reduce fragmentation
     */
    private void checkAndRunCompaction () {
        if (getDiskUtilization() < this.config.getCompactionThreshold()) {
            return;
        }
        int currentBlockNumber = 1;
        int currentIndexInBlock = 0;  // Index in the current block

        // Iterate through blocks and process tombstones
        for (int blockNumber = 1; blockNumber <= occupiedBlocks; blockNumber++) {
            Block currentBlock = disk.getBlock(blockNumber);
            int numRecords = currentBlock.getRecordCount();

            // Iterate through records in the current block
            for (int j = 0; j < numRecords; j++) {
                Record record = currentBlock.getRecordAt(j);
                if (record.isTombstone()) {
                    numRecords--;
                    continue;
                }
                // Move valid records to the current block and index in block
                Block block = disk.getBlock(currentBlockNumber);
                block.insertRecordAt(currentIndexInBlock, record);
                currentIndexInBlock++;

                if (block.isFull()) {
                    currentBlockNumber++;
                    currentIndexInBlock = 0;
                }
            }
        }
    }

    public void printState (Boolean verbose){
        System.out.println("#####\tPrinting state of Storage\t#####");
        if (verbose) {
            for (int blockNumber = 1; blockNumber <= occupiedBlocks; blockNumber++) {
                System.out.println(disk.getBlock(blockNumber));
            }
        }
        System.out.println(String.format("Number of records: %d", numRecords));
        System.out.println(String.format("Size of record in bytes: %d", Record.RECORD_BYTE_SIZE));
        System.out.println(String.format("Max number of records in a block: %d", Block.MAX_RECORDS_PER_BLOCK));
        System.out.println(String.format("Current number of occupied blocks: %d", occupiedBlocks));
        System.out.println(String.format("Disk utilization: %f %%", getDiskUtilization() * 100));
        System.out.println();
    }

    private void printStatistics (String method,int blockAccessCounter, Double averageRating,long duration){
        System.out.println(method + " Method:");
        System.out.println(String.format("No. of Block Access: %d", blockAccessCounter));
        if (averageRating != null) {
            System.out.println(String.format("Average Rating: %.2f", averageRating));
        }
        System.out.println(String.format("Running Time (ms): %d", duration));
        System.out.println();
    }
}