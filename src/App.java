public class App {

    public static StorageManager initOrResetDisk() {
        // Init disk memory
        Disk disk = new Disk(Block.BLOCK_BYTE_SIZE);

        // Initialize default B tree using numVotes
        BPlusTree bPlusTree = new BPlusTree(200);

        // Init storage related components
        WriteAheadLog writeAheadLog = new WriteAheadLog(System.getProperty("user.dir") + "/recovery.log");
        StorageConfiguration storageConfiguration = new StorageConfiguration.Builder().build();
        StorageManager storageManager = new StorageManager(disk, storageConfiguration, writeAheadLog, bPlusTree);

        // Seed data
        DataSeeder.seed(System.getProperty("user.dir") + "/data.tsv", storageManager);

        return storageManager;
    }

    public static void main(String[] args) throws Exception {

        StorageManager storageManager = initOrResetDisk();

        // Experiment 1
        storageManager.printState(false);

        // Experiment 3
        System.out.println("Experiment 3: Retrieving records by number of votes");
        storageManager.retrieveRecordsByNumVotes(500); // B+ tree method
        storageManager.linearScanByNumVotes(500); // Linear scan method
        System.out.println();
        
        // Experiment 4
        System.out.println("Experiment 4: Retrieving records by number of votes range");
        storageManager.retrieveRecordsByNumVotesRange(30000, 40000); // B+ tree method
        storageManager.linearScanByNumVotesRange(30000, 40000); // Linear scan method
        System.out.println();

        // Experiment 5
        System.out.println("Experiment 5: Deleting records by number of votes");
        storageManager.deleteRecordsByNumVotes(1000); // B+ tree method
        System.out.println("*****Resetting state of disk before brute-force deletion...*****");
        System.out.println();
        StorageManager storageManager2 = initOrResetDisk(); // Reset disk before another deletion
        storageManager2.printState(false);
        storageManager2.linearScanDeleteByNumVotes(1000); // Linear scan method
        System.out.println();
    }
}
