import java.util.ArrayList;

public class ParentNode extends Node {
    private ArrayList<Node> children;

    public ParentNode() {
        super();
        children = new ArrayList<Node>();
    }

    // Getters and setters

    /**
     * Getter for the children field
     */
    public ArrayList<Node> returnChildren(){
        return this.children;
    }

    /**
     * Returns the child node at the specified index
     */
    public Node returnChild(int ind){
        return this.children.get(ind);
    }

    /**
     * Appends a child node to the arraylist of children
     * If the parent has existing children, it adds the new child in the appropriate position based on key values
     * Otherwise, it simply adds the child to the list
     */
    public void appendChild(Node child){
        int size = this.children.size();

        if(size != 0){
            int k = child.returnSmallest(); // Get the smallest key from the new child
            int s = this.returnSmallest(); // Get the smallest key from the parent's existing children
            int ind;

            // Determine the position to insert the new child based on key values
            if (k >= s) {
                this.children.add(this.appendKey(k) + 1, child);   
            }
            else {
                ind = 0;
                this.appendKey(s);
                this.children.add(0, child);
            }

            child.setParent(this); // Set the parent of the new child
        }
        else{
            children.add(child);
            child.setParent(this); // Set the parent of the new child
        }
    }

    /**
     * Appends a child node to the arraylist of children at index 0
     * Used when splitting a parent node and promoting a new parent
     */
    public void appendChild(Node child, int ind){
        children.add(0, child);
        child.setParent(this); // Set the parent of the new child
        deleteKeys(); // Clear the existing keys
        int size = children.size();
        
        // Rebuild the keys list based on the children
        for (int j = 0; j < size; j++) {
            if (j != 0){
                int smallest = children.get(j).returnSmallest();
                appendKey(smallest);
            }
        }
    }

    /**
     * Remove all children
     */
    public void deleteChildren() {
        children = new ArrayList<Node>(); // Create a new empty ArrayList to clear the children
    }

    /**
     * Removes the specified child node
     * Updates the keys list after removing the child
     */
    public void removeChild(Node child){
        children.remove(child); // Remove the child from the list of children
        deleteKeys(); // Clear the existing keys
        int size = children.size();
        for (int j = 1; j < size; j++) {
            int smallest = children.get(j).returnSmallest();
            appendKey(smallest); // Update the keys list based on the remaining children
        }
    }

    /**
     * Returns the child node before the specified node
     */
    public Node returnChildBefore(Node node){
        int ind = children.indexOf(node);
        if (ind == 0){return null;} // If the specified node is the first child, there is no child before it
        else{return children.get(children.indexOf(node)-1);} // Otherwise, return the node before the specified node
    }

    /**
     * Returns the child node after the specified node
     */
    public Node returnChildAfter(Node node){
        int ind = children.indexOf(node);
        int maxInd = children.size()-1;
        if (ind == maxInd){return null;} // If the specified node is the last child, there is no child after it
        else{return children.get(children.indexOf(node)+1);} // Otherwise, return the node after the specified node
    }  
}
