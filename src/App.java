public class App {
    public static void main(String[] args) throws Exception {
        // Init disk memory
        Disk disk = new Disk(Block.BLOCK_BYTE_SIZE);
        // Init storage related components
        WriteAheadLog writeAheadLog = new WriteAheadLog(System.getProperty("user.dir") + "/wal.tsv");
        StorageConfiguration storageConfiguration = new StorageConfiguration.Builder().build();
        StorageManager blockManager = new StorageManager(disk, storageConfiguration, writeAheadLog);

        // seed data;
        DataSeeder.seed(System.getProperty("user.dir") + "/data.tsv", blockManager);
        blockManager.printState(false);

        // Initialize default B tree using uuid(primary key)

        // Initialize any other indexes the user has created
        
        // answer queries

    }
}
