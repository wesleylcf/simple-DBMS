import java.util.*;

public class Node {
    private ArrayList<Integer> keys;
    private ParentNode parent;
    private boolean isLeaf;
    private boolean isRoot;

    public Node() {
        this.keys = new ArrayList<Integer>();
        this.isLeaf = false;
        this.isRoot = false;
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
        ParentNode copy;
        boolean isLeaf = this.returnLeaf();

        if (!isLeaf) {
            copy = (ParentNode) this;

            while (!copy.returnChild(0).returnLeaf())
                copy = (ParentNode) copy.returnChild(0);
            
            key = copy.returnChild(0).returnKey(0);
        } else {
            key = this.returnKey(0);
        } 
        return key;
    }

    /**
     * Removes node
     */
    public void removeNode() {

        if (parent != null) {

            ((ParentNode) parent).removeChild(this);
            parent = null;
        }

        if (this.isLeaf) {
            LeafNode copy = (LeafNode) this;
            copy.deleteRecords();
            copy.setNext(null);
        }
        else {
            ParentNode copy = (ParentNode) this;
            copy.removeChild(copy);

        }

        this.isLeaf = false;
        this.isRoot = false;
        keys = new ArrayList<Integer>();
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
    public ParentNode returnParent() {
        return parent;
    }

    /**
     * Setter for parent field
     */
    public void setParent(ParentNode pn) {
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
        this.keys.remove(index);
    }

    // public void insertChildToFront(Node newChild) {
    //     // Insert this new child to the front.
    //     children.add(0, newChild);
    //     newChild.setParent(this);
    //     deleteKeys();

    //     int childNodesSetSize = children.size();
    //     int targetKeyIndex = -1;
    //     // Adjust the keys.
    //     for (int ptr = 1; ptr < childNodesSetSize; ptr++) {
    //         targetKeyIndex = children.get(ptr).returnSmallest();
    //         appendKey(targetKeyIndex);
    //     }
    // }
}