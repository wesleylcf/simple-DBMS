import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Repository {
  List<Record> records;

  public Repository(String filePath) {
    records = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      br.readLine(); // skip header row

      while ((line = br.readLine()) != null) {
          String[] parts = line.split("\t");

          if (parts.length == 3) {
              String tconst = parts[0];
              Float averageRating = Float.parseFloat(parts[1]);
              Integer numVotes = Integer.parseInt(parts[2]);

              Record record = new Record(tconst, averageRating, numVotes);
              records.add(record);
          } else {
              System.err.println(String.format("Expected 3 columns but received: %s", parts));
          }
      }
    } catch (IOException e) {
        System.err.println(String.format("Could not read file at filePath %s", filePath));
        e.printStackTrace(); // Handle the exception according to your needs
    }
  }

  public Record getRecordAtIndex(Integer index) {
    return records.get(index);
  }

  /*
   * 
   */
  public void printHead() {
    for(Integer i = 0; i < 10; i++) {
      System.out.println(records.get(i));
    }
  } 
}
