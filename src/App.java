public class App {
    public static void main(String[] args) throws Exception {
        String filePath = System.getProperty("user.dir") + "/data.tsv";
        Repository repo = new Repository(filePath);
        repo.printHead();
    }
}
