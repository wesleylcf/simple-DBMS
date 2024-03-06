public class Address {
    int id; // Block ID
    int offset;

    public Address(int id, int off){
        this.id=id;
        this.offset=off;
    }

    public int returnId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}