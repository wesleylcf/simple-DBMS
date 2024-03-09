import java.util.*;

public class Node {
    private ArrayList<Integer> keys;
    private ArrayList<Node> children;
    private ArrayList<Address> records;
    private Node parent;
    private Node next;  // For leafnode
    private boolean isLeaf;
    private boolean isRoot;
    private boolean isParent;

    public Node(boolean isParent, boolean isLeaf) {
        this.keys = new ArrayList<Integer>();
        this.isLeaf = isLeaf;
        this.isRoot = false;
        this.isParent = isParent;
        if(isParent){this.children = new ArrayList<Node>();}
        if(isLeaf){records = new ArrayList<Address>();}
    }

    /**
     * Add key to keys list
     */
    public int appendKey(int key) {

        int len = this.returnKeys().size();

        if (len != 0) {
            int i;
            keys.add(key);
            for (i = keys.size() -2; i >= 0; i--) {

                if (keys.get(i) <= key) {

                    i++;
                    keys.set(i, key);
                    break;
                }

                keys.set(i+1, keys.get(i));
                if (i == 0) {

                    keys.set(i, key);
                    break;
                }
            }
            return i;
        }else{
            this.keys.add(key);
            return 0;
        }
    }

    /**
     * Return smallest key. Mostly used by the parent node, but is inside this class in case of 1st level parents
     */
    public int returnSmallest() {

        int key;
        Node copy;
        boolean isLeaf = this.returnLeaf();

        if (!isLeaf) {

            copy = this;

            while (!copy.returnChild(0).returnLeaf())
                copy = copy.returnChild(0);
            
            key = copy.returnChild(0).returnKey(0);
        }
        else{key = this.returnKey(0);} 
        return key;
    }

    /**
     * Removes node
     */
    public void removeNode() {

        if (parent != null) {

            parent.removeChild(this);
            parent = null;
        }

        if (this.isLeaf) {
            Node copy = this;
            copy.records = new ArrayList<Address>();
            copy.setNext(null);
        }
        else {
            if(this.isParent == false){System.out.println("Node needs ot be a parent.");}
            else{
                Node copy = this;
                copy.children = new ArrayList<Node>();
            }
        }

        this.isLeaf = false;
        this.isRoot = false;
        keys = new ArrayList<Integer>();
    }

    /**
     * Appends child node to arraylist of children
     */
    public void appendChild(Node child){
        int size = this.children.size();

        if(size != 0){
            int k = child.returnSmallest();
            int s = this.returnSmallest();
            int ind;

            if (k >= s) {
                this.children.add(this.appendKey(k) +1, child);   
            }
            else {
                ind = 0;
                this.appendKey(s);
                this.children.add(0, child);
            }

            child.setParent(this);
        }
        else{
            children.add(child);
            child.setParent(this);
        }
    }

    /**
     * Appends child node to arraylist of children, at index 0
     */
    public void appendChild(Node child, int ind){
        children.add(0, child);
        child.setParent(this);
        this.keys = new ArrayList<Integer>();
        int size = children.size();
        
        for (int j = 0; j < size; j++) {
            if (j != 0){
                int smallest = children.get(j).returnSmallest();
                appendKey(smallest);
            }
        }
    }

    /**
     * Remove specified child node
     */
    public void removeChild(Node child){
        children.remove(child);
        this.keys = new ArrayList<Integer>();
        int size = this.children.size();
        for (int j = 0; j < size; j++) {
            if (j != 0){
                int smallest = children.get(j).returnSmallest();
                appendKey(smallest);
            }
        }
    }

    /**
     * Returns child node before
     */
    public Node returnChildBefore(Node node){
        int ind = children.indexOf(node);
        if (ind == 0){return null;}
        else{return children.get(children.indexOf(node)-1);}
    }

    /**
     * Returns child node after
     */
    public Node returnChildAfter(Node node){
        int ind = children.indexOf(node);
        int maxInd = children.size()-1;
        if (ind == maxInd){return null;}
        else{return children.get(children.indexOf(node)+1);}
    }

    // Getters and Setters

    /**
     * Getter for isLeaf
     field
     */
    public boolean returnLeaf() {
        return this.isLeaf;
    }

    /**
     * Setter for isLeaf
     field
     */
    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    /**
     * Getter for isRoot field
     */
    public boolean returnRoot() {
        return this.isRoot;
    }

    /**
     * Setter for isRoot field
     */
    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    /**
     * Getter for parent field
     */
    public Node returnParent() {
        return this.parent;
    }

    /**
     * Setter for parent field
     */
    public void setParent(Node pn) {
        parent = pn;
    }

    /**
     * Obtain key of given index
     */
    public int returnKey(int ind) {
        return keys.get(ind);
    }

    /**
     * Getter for keys field
     */
    public ArrayList<Integer> returnKeys() {
        return this.keys;
    }

    /**
     * Getter for children field
     */
    public ArrayList<Node> returnChildren(){
        return this.children;
    }

    /**
     * Return child node of specified index
     */
    public Node returnChild(int ind){
        return this.children.get(ind);
    }

    /**
     * Returns records field
     */
    public ArrayList<Address> returnRecords(){
        return this.records;
    }

    /**
     * Returns record of specified index
     */
    public Address returnRecord(int ind){
        return this.records.get(ind);
    }

    /**
     * Adds records to address
     */
    public int addRecord(int key, Address address) {
        if (this.returnRecords().size() == 0) {

            this.records.add(address);
            this.appendKey(key);
            return 0;
        }

        int index;
        index = appendKey(key);
        records.add(address);

        for (int i = records.size() -2; i >= index; i--) 
            records.set(i+1, records.get(i));
        
        records.set(index, address);

        return index;
    }

    /**
     * Returns next isLeaf
     node
     */
    public Node returnNext() {
        return this.next;
    }

    /**
     * Setter for next leafnode
     */
    public void setNext(Node node) {
        this.next = node;
    }

    /**
     * Delete a record at a specified index
     */
    public void deleteRecord(int index) {
        // Delete the record using the index
        deleteKey(index);
        // Remove the record from the records list
        records.remove(index);
    }

    /**
     * Deletes all records from the node.
     */
    public void deleteRecords() {
        // Reset the records list
        this.records = new ArrayList<Address>();
    }

    /**
     * Deletes all keys from the node.
     */
    public void deleteKeys() {
        // Reset the keys list
        this.keys = new ArrayList<Integer>();
    }

    /**
     * Deletes a key at a specified index
     */
    public void deleteKey(int index) {
        // Delete the key using the index
        keys.remove(index);
    }
}