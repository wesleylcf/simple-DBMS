public class App {
    public static void main(String[] args) throws Exception {
        // Read records and store in RecordManager
        String filePath = System.getProperty("user.dir") + "/data.tsv";
        RecordManager recordManager = new RecordManager(filePath);
        recordManager.printHead();
        // Init disk memory
        Disk disk = new Disk(Block.BLOCK_BYTE_SIZE);
        // Put seed data into Blocks
        StorageManager blockManager = new StorageManager(recordManager.records, disk);
        blockManager.printState(false);
        // Initialize default B tree using uuid(primary key)

        // Initialize any other indexes the user has created
        
        // answer queries

    }
}
