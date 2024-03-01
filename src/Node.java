import java.util.*;

public class Node {
    
    private ArrayList<Integer> keys;
    private ArrayList<Integer> children;
    // private ParentNode parent;
    private Node parent;
    private boolean leaf;
    private boolean root;
    private String label;

    public Node() {
        keys = new ArrayList<Integer>();
        leaf = false;
        root = false;
    }
    
    /**
     * Getter for leaf field
     */
    public boolean getleaf() {
        return this.leaf;
    }

    /**
     * Setter for leaf field
     */
    public void setleaf(boolean leaf) {
        this.leaf = leaf;
    }

    /**
     * Getter for root field
     */
    public boolean getroot() {
        return this.root;
    }

    /**
     * Setter for root field
     */
    public void setroot(boolean root) {
        root = root;
    }

    /**
     * Getter for parent field
     */
    public ParentNode getParent() {
        return this.parent;
    }

    /**
     * Setter for parent field
     */
    public void setParent(ParentNode pn) {
        parent = pn;
    }

    /**
     * Getter for keys field
     */
    public ArrayList<Integer> returnKeys() {
        return this.keys;
    }

    /**
     * Obtain key of given index
     */
    public int returnKey(int ind) {
        return keys.get(ind);
    }

    /**
     * Add key to keys list
     */
    public int addKey(int key) {

        int len = this.returnKeys().size();

        if (len == 0) {
            this.keys.add(key);
            return 0;
        }

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
    }

    /**
     * Remove key from specified index
     */
    public void returnKey(int ind) {

        keys.remove(ind);
    }

    /**
     * Removes all keys prior to splitting
     */
    public void returnKeys() {

        keys = new ArrayList<Integer>();
    }

    // find smallest key (more for use by parentnode but placed here for first level of parents)
    public int findSmallestKey() {

        int key;
        ParentNode copy;
        boolean isLeaf = this.getLeaf();

        if (!isLeaf) {

            copy = (ParentNode) this;

            while (!copy.getChild(0).getleaf())
                copy = (ParentNode) copy.getChild(0);
            
            key = copy.getChild(0).returnKey(0);
        }

        else 
            key = this.returnKey(0);

        return key;
    }

    // delete the node
    public void deleteNode() {

        if (parent != null) {

            parent.deleteChild(this);
            parent = null;
        }

        if (this.leaf) {
            
            LeafNode copy = (LeafNode) this;
            copy.deleteRecords();
            copy.setNext(null);
        }

        else {

            ParentNode copy = (ParentNode) this;
            copy.deleteChildren();
        }

        leaf = false;
        root = false;
        keys = new ArrayList<Integer>();
    }

    abstract void logStructure();
}