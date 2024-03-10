import java.util.ArrayList;

public class LeafNode extends Node {

    private ArrayList<Address> records;
    private LeafNode next;
    
    public LeafNode() {
        super();
        records = new ArrayList<Address>();
        setLeaf(true);
        setNext(null);
    }

    // Getters and setters
    /**
     * Returns the records field
     */
    public ArrayList<Address> returnRecords(){
        return this.records;
    }

    /**
     * Returns the record at the specified index
     */
    public Address returnRecord(int ind){
        return this.records.get(ind);
    }

    /**
     * Adds a record to the node with the specified key and address
     * Returns the index where the record was added
     */
    public int addRecord(int key, Address address) {
        if (this.returnRecords().size() == 0) {
            this.records.add(address);
            this.appendKey(key);
            return 0;
        }

        int index = appendKey(key);
        records.add(address);

        for (int i = records.size() - 2; i >= index; i--) 
            records.set(i + 1, records.get(i));
        
        records.set(index, address);

        return index;
    }

    /**
     * Returns the next leaf node
     */
    public LeafNode returnNext() {
        return this.next;
    }

    /**
     * Sets the next leaf node
     */
    public void setNext(LeafNode node) {
        this.next = node;
    }

    /**
     * Deletes the record at the specified index
     */
    public void deleteRecord(int index) {
        deleteKey(index);  // Delete the key associated with the record
        records.remove(index);  // Remove the record from the records list
    }

    /**
     * Deletes all records from the node.
     */
    public void deleteRecords() {
        this.records = new ArrayList<Address>();  // Reset the records list
    }
}
