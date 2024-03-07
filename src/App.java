public class App {
    public static void main(String[] args) throws Exception {
        // Init disk memory
        Disk disk = new Disk(Block.BLOCK_BYTE_SIZE);

        // Initialize default B tree using uuid(primary key)
        BPlusTree bPlusTree = new BPlusTree(200);

        // Init storage related components
        WriteAheadLog writeAheadLog = new WriteAheadLog(System.getProperty("user.dir") + "/recovery.log");
        StorageConfiguration storageConfiguration = new StorageConfiguration.Builder().build();
        StorageManager storageManager = new StorageManager(disk, storageConfiguration, writeAheadLog, bPlusTree);

        // seed data;
        DataSeeder.seed(System.getProperty("user.dir") + "/data.tsv", storageManager);
        storageManager.printState(false);

        // Initialize any other indexes the user has created

        // answer queries


        // Experiment 3
        System.out.println("Experiment 3: Retrieving records by number of votes");
        storageManager.retrieveRecordsByNumVotes(500); // B+ tree method
        storageManager.linearScanByNumVotes(500); // Linear scan method
        System.out.println();
        
        // Experiment 4
        System.out.println("Experiment 4: Retrieving records by number of votes range");
        storageManager.retrieveRecordsByNumVotesRange(30000, 4000); // B+ tree method
        storageManager.linearScanByNumVotesRange(30000, 4000); // Linear scan method
    }
}
