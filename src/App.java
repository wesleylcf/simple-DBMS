public class App {
    public static void main(String[] args) throws Exception {
        String filePath = System.getProperty("user.dir") + "/data.tsv";
        RecordManager repo = new RecordManager(filePath);
        repo.printHead();
    }
}
