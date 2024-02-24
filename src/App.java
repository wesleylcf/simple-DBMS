public class App {
    public static void main(String[] args) throws Exception {
        // Read records and store in RecordManager
        String filePath = System.getProperty("user.dir") + "/data.tsv";
        RecordManager recordManager = new RecordManager(filePath);
        recordManager.printHead();
        // Put seed data into Blocks
        BlockManager blockManager = new BlockManager(recordManager.records);
        blockManager.printState(false);
        // Initialize default B tree using uuid(primary key)

        // Initialize any other indexes the user has created
        
        // answer queries

    }
}
