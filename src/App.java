public class App {
    public static void main(String[] args) throws Exception {
        // Init disk memory
        Disk disk = new Disk(Block.BLOCK_BYTE_SIZE);
        StorageManager blockManager = new StorageManager(disk);

        // seed data
        String filePath = System.getProperty("user.dir") + "/data.tsv";
        DataSeeder.seed(filePath, blockManager);
        blockManager.printState(false);

        // Initialize default B tree using uuid(primary key)

        // Initialize any other indexes the user has created
        
        // answer queries

    }
}
