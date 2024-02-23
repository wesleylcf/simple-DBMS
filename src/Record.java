public class Record {
  private String uuid;
  private Float averageRating;
  private Integer numVotes;
  
  private Byte padding1;
  private Byte padding2;

  public Record(String tconst, Float averageRating, Integer numVotes) {
    this.uuid = tconst;
    this.averageRating = averageRating;
    this.numVotes = numVotes;
  }

  public String getUuid() {
    return uuid;
  }

  public Float getAverageRating() {
    return averageRating;
  }

  public Integer getNumVotes() {
    return numVotes;
  }

  @Override
  public String toString() {
      return String.format("{ uuid:'%s', averageRating:%f, numVotes:%d }", this.uuid, this.averageRating, this.numVotes);
  }
}