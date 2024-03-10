import java.util.ArrayList;

public class BPlusTree {
    private static final int POINTER_SIZE = 8;
    private static final int KEY_SIZE = 4;

    int maxKeys;
    int minKeysParentNode;
    int minKeysLeafNode;
    int height;
    int nodeCount;
    int deletedCount;
    int recordCount;
    Node root;

    public BPlusTree(int blockSize) {
        // Calculate the maximum number of keys that can fit in a block
        maxKeys = (blockSize - POINTER_SIZE) / (KEY_SIZE + POINTER_SIZE);
        
        // Calculate the minimum number of keys for parent and leaf nodes
        minKeysParentNode = (int) Math.floor(maxKeys / 2);
        minKeysLeafNode = (int) Math.floor((maxKeys + 1) / 2);
        
        // Create the root node and initialize the tree
        root = createTree();
        nodeCount = 0;
        deletedCount = 0;
        
        System.out.println("B+ tree initialized with maxKeys = " + maxKeys + ", minKeysParentNode = " + minKeysParentNode + ", minKeysLeafNode = " + minKeysLeafNode);
    }

    /**
     * Creates a new B+ tree with a single leaf node as the root.
     * 
     * @return The root node of the B+ tree.
     */
    public Node createTree() {
        // Create a new root node, set it as leaf, and mark it as the root of the tree
        LeafNode root = new LeafNode();
        root.setRoot(true);

        // Set the tree height to 1 and initialize the node count
        height = 1;
        nodeCount = 1;

        // Return the newly created root node
        return root;
    }

    /**
    * Inserts a key-value pair into the B+ tree.
    * If the leaf node is full, it splits the leaf node and redistributes the keys.
    * @param key The key to insert.
    * @param address The address associated with the key.
    */
    public void insert(int key, Address address) {
        // Find the leaf node where the key should be inserted
        LeafNode leafNode = new LeafNode();
        leafNode = this.searchLeaf(key);

        // If the leaf node is not full, add the record directly
        if (leafNode.returnKeys().size() < maxKeys){
            leafNode.addRecord(key, address);
        } else {
            // If the leaf node is full, split it
            splitLeaf(leafNode, key, address);
        }
    }

    /**
    * Searches for the leaf node where the key should be inserted.
    * @param key The key to search for.
    * @return The leaf node where the key should be inserted.
    */
    public LeafNode searchLeaf(int key) {
        // If the root node is a leaf, return it directly
        if (this.root.returnLeaf())
            return (LeafNode) root;

        ParentNode parentNode = (ParentNode) root;
        ArrayList<Integer> keys;

        // Traverse the tree to find the appropriate leaf node
        while (!parentNode.returnChild(0).returnLeaf()) {
            keys = parentNode.returnKeys();

            // Search for the appropriate child node based on the key
            for (int i = keys.size() - 1; i >= 0; i--) {
                if (keys.get(i) <= key) {
                    parentNode = (ParentNode) parentNode.returnChild(i + 1);
                    break;
                } else if (i == 0) parentNode = (ParentNode) parentNode.returnChild(0);
            }
        }

        keys = parentNode.returnKeys();
        for (int i = keys.size() - 1; i >= 0; i--) {
        // Search for the leaf node where the key should be inserted
            if (keys.get(i) <= key)
                return (LeafNode) parentNode.returnChild(i + 1);
        }
        return (LeafNode) parentNode.returnChild(0);
    }

    /**
    * Splits a leaf node when it is full and redistributes the keys and addresses.
    * @param originalNode The original leaf node to be split.
    * @param key The key to be inserted into the leaf node.
    * @param address The address associated with the key.
    */
    public void splitLeaf(LeafNode originalNode, int key, Address address) {
        int i;
        int keys[] = new int[maxKeys + 1];
        Address addresses[] = new Address[maxKeys + 1];
        LeafNode newLeaf = new LeafNode();

        // Copying keys and addresses from the original node to arrays
        for (i = 0; i < maxKeys; i++) {
            keys[i] = originalNode.returnKey(i);
            addresses[i] = originalNode.returnRecord(i);
        }

        // Inserting the new key and address into the appropriate position in the arrays
        for (i = maxKeys - 1; i >= 0; i--) {
            if (keys[i] <= key) {
                i++;
                keys[i] = key;
                addresses[i] = address;
                break;
            }
            keys[i + 1] = keys[i];
            addresses[i + 1] = addresses[i];
        }

        // Clearing the old keys and addresses from the original node
        originalNode.deleteKeys();
        originalNode.deleteRecords();

        // Adding keys and addresses to the original and new leaf nodes
        for (i = 0; i < minKeysLeafNode; i++)
            originalNode.addRecord(keys[i], addresses[i]);

        for (i = minKeysLeafNode; i < maxKeys + 1; i++)
            newLeaf.addRecord(keys[i], addresses[i]);

        // setting old leafnode to point to new leafnode and new leafnode to point to
        newLeaf.setNext(originalNode.returnNext());
        originalNode.setNext(newLeaf);

        // Setting the next pointer of the original node to point to the new leaf node
        if (originalNode.returnRoot()) {
            ParentNode newRoot = new ParentNode();
            originalNode.setRoot(false);
            newRoot.setRoot(true);
            newRoot.appendChild(originalNode);
            newRoot.appendChild(newLeaf);
            root = newRoot;
            height++;
        } else if (originalNode.returnParent().returnKeys().size() < maxKeys)
            // If the parent node has space, add the new leaf node to it
            originalNode.returnParent().appendChild(newLeaf);
        else {
            // If the parent node is full, split it
            splitParent(originalNode.returnParent(), newLeaf);
        }
        // Increase nodeCount
        nodeCount++;
    }

    /**
     * Splits a parent node when it is full and redistributes the keys and child
     * nodes.
     * 
     * @param parentNode The parent node to be split.
     * @param childNode  The child node to be inserted into the parent.
     */
    public void splitParent(ParentNode parentNode, Node childNode) {
        // Create arrays to hold child nodes and keys
        Node children[] = new Node[maxKeys + 2];
        int keys[] = new int[maxKeys + 2];

        // Initialize a new parent node
        ParentNode newParentNode = new ParentNode();
        int key = childNode.returnSmallest();

        // Retrieve full and sorted lists of keys and children from the parent node
        for (int i = 0; i < maxKeys + 1; i++) {
            children[i] = parentNode.returnChild(i);
            keys[i] = children[i].returnSmallest();
        }

        // Insert the new child node into the appropriate position in the arrays
        for (int i = maxKeys; i >= 0; i--) {
            if (keys[i] <= key) {
                i++;
                keys[i] = key;
                children[i] = childNode;
                break;
            }
            keys[i + 1] = keys[i];
            children[i + 1] = children[i];
        }

        // Clear old keys and child nodes from the parent node
        parentNode.deleteKeys();
        parentNode.deleteChildren();

        // Distribute the children between the original and new parent nodes
        for (int i = 0; i < minKeysParentNode + 2; i++)
            parentNode.appendChild(children[i]);

        for (int i = minKeysParentNode + 2; i < maxKeys + 2; i++)
            newParentNode.appendChild(children[i]);

        // Set parent for the new parent node
        if (parentNode.returnRoot()) {
            // If the original parent is the root, create a new root
            ParentNode newRoot = new ParentNode();
            parentNode.setRoot(false);
            newRoot.setRoot(true);
            newRoot.appendChild(parentNode);
            newRoot.appendChild(newParentNode);
            this.root = newRoot;
            height++;
        } else if (parentNode.returnParent().returnKeys().size() < maxKeys) {
            // If the parent of the original parent has space, add the new parent node to it
            parentNode.returnParent().appendChild(newParentNode);
        } else {
            // If the parent of the original parent is full, split it recursively
            splitParent(parentNode.returnParent(), newParentNode);
        }

        // Update node count
        nodeCount++;
    }

    /**
     * Deletes the records with the specified key from the B+ tree.
     * 
     * @param key The key of the records to be deleted.
     */
    public void deleteKey(int key) {
        ArrayList<Integer> keys;
        LeafNode leaf;

        // Loop until all records with the given key value are deleted
        while (getRecordsWithKey(key).size() != 0) {
            // Search for the leaf node containing the key
            leaf = searchLeaf(key);
            keys = leaf.returnKeys();

            // Delete one record and update the tree
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i) == key) {
                    leaf.deleteRecord(i);

                    // If the node is not the root, update the tree
                    if (!leaf.returnRoot())
                        resetLeaf(leaf);
                    break;
                }
            }
            if (getRecordsWithKey(key).size() == 1) break;
        }
        // Update node count
        //System.out.println("Deletion: number of nodes deleted = " + deletedCount);
        nodeCount -= deletedCount;
        //bPlusTreeStats(); // Update B+ tree statistics
    }
   
    /**
     * Resets the leaf node by redistributing keys or merging with neighboring nodes
     * if necessary.
     * 
     * @param node The leaf node to be reset.
     */
    public void resetLeaf(LeafNode node) {
        // If the node already has enough keys, reset its parent and finish
        if (node.returnKeys().size() >= minKeysLeafNode) {
            //("case 1 enough keys");
            resetParent(node.returnParent());
            return;
        }

        // Get the neighboring nodes
        LeafNode left = (LeafNode) node.returnParent().returnChildBefore(node);
        LeafNode right = (LeafNode) node.returnParent().returnChildAfter(node);

        int needed = minKeysLeafNode - node.returnKeys().size(); // Number of keys needed to fill the node
        int leftSpare = 0; // Number of keys the before node can spare
        int rightSpare = 0; // Number of keys the after node can spare
        ParentNode copyParent; // Copy of the parent node for updating

        // Calculate the number of keys that the before and after nodes can spare
        if (left != null)
            leftSpare += left.returnKeys().size() - minKeysLeafNode;

        if (right != null)
            rightSpare += right.returnKeys().size() - minKeysLeafNode;

        // If merging is necessary
        if (needed > rightSpare + leftSpare) {
            // Merge keys into the before and after nodes
            if (left != null && right != null) {
                // Fill the before node with keys from the current node
                for (int i = 0; i < maxKeys - (leftSpare + minKeysLeafNode); i++)
                    if (i<node.returnKeys().size())
                        left.addRecord(node.returnKey(i), node.returnRecord(i));
                // Fill the rest into the after node
                for (int i = maxKeys - (leftSpare + minKeysLeafNode); i < node.returnKeys().size(); i++)
                    right.addRecord(node.returnKey(i), node.returnRecord(i));
            } else if (left == null) {
                // Add keys to the after node if the current node has no before node
                for (int i = 0; i < node.returnKeys().size(); i++)
                    right.addRecord(node.returnKey(i), node.returnRecord(i));
            } else {
                // Add keys to the before node if the current node has no after node
                for (int i = 0; i < node.returnKeys().size(); i++)
                    left.addRecord(node.returnKey(i), node.returnRecord(i));
            }
            // Copy the parent node for resetting after deleting the leaf node
            copyParent = node.returnParent();
            // Adjust the before node if it's from a different parent
            if (left == null) {
                if (!copyParent.returnRoot())
                    left = searchLeaf(copyParent.returnSmallest() - 1);
            }
            // Redirect the before node to the after node
            left.setNext(node.returnNext());
            // Delete the current node
            node.removeNode();
            deletedCount++;
        } else { // If borrowing keys is possible
            if (left != null && right != null) {
                // Borrow keys from the before node and the after node
                for (int i = 0; i < leftSpare; i++) {
                    node.addRecord(left.returnKey(left.returnKeys().size() - 1 - i),left.returnRecord(left.returnKeys().size() - 1 - i));
                    left.deleteRecord(left.returnKeys().size() - 1 - i);
                }
                for (int i = leftSpare, j = 0; i < needed; i++, j++) {
                    node.addRecord(right.returnKey(j), right.returnRecord(j));
                    right.deleteRecord(j);
                }
            } else if (left == null) {
                // Borrow all keys from the after node if there's no before node
                for (int i = 0; i < needed; i++) {
                    node.addRecord(right.returnKey(i), right.returnRecord(i));
                    right.deleteRecord(i);
                }
            } else {
                // Borrow all keys from the before node if there's no after node
                for (int i = 0; i < needed; i++) {
                    node.addRecord(left.returnKey(left.returnKeys().size() - 1 - i),
                            left.returnRecord(left.returnKeys().size() - 1 - i));
                    left.deleteRecord(left.returnKeys().size() - 1 - i);
                }
            }
            copyParent = node.returnParent();
        }
        // Update the parents after resetting
        //System.out.println("final case");
        resetParent(copyParent);
    }

    /**
     * Resets the parent node by redistributing keys or merging with neighboring
     * nodes if necessary.
     * 
     * @param parent The parent node to be reset.
     */
    public void resetParent(ParentNode parent) {
        // If the node is a root node
        if (parent.returnRoot()) {
            // If the root has at least two children, simply reorganize them and return
            if (parent.returnChildren().size() > 1) {
                // Move the first child to the end
                Node child = parent.returnChild(0);
                parent.removeChild(child);
                parent.appendChild(child);
                return;
            }
            // If the root has only one child, eliminate the root level
            else {
                parent.returnChild(0).setRoot(true);
                root = parent.returnChild(0);
                parent.removeNode();
                deletedCount++;
                height--;
                return;
            }
        }
        ParentNode left = (ParentNode) parent.returnParent().returnChildBefore(parent);
        ParentNode right = (ParentNode) parent.returnParent().returnChildAfter(parent);

        int needed = minKeysLeafNode - parent.returnKeys().size(); // Number of keys needed to fill the node
        int leftSpare = 0; // Number of keys the before node can spare
        int rightSpare = 0; // Number of keys the after node can spare
        ParentNode copy; // Copy of the parent node for updating

        if (left != null)
            leftSpare += left.returnKeys().size() - minKeysParentNode;

        if (right != null)
            rightSpare += right.returnKeys().size() - minKeysParentNode;

        // If merging is necessary
        if (needed > rightSpare + leftSpare) {
            // Merge keys into the before and after nodes
            if (left != null && right != null) {
                // Transfer as many records as possible to the before node
                for (int i = 0; i < maxKeys - (leftSpare + minKeysParentNode) + 1 && i < parent.returnChildren().size(); i++)
                    left.appendChild(parent.returnChild(i));

                // Transfer the remaining records to the after node
                for (int i = maxKeys - (leftSpare + minKeysParentNode) + 1; i < parent.returnChildren().size(); i++)
                    right.appendChild(parent.returnChild(i));
            }
            // If only the after node is available
            else if (left == null) {
                for (int i = 0; i < parent.returnChildren().size(); i++)
                    right.appendChild(parent.returnChild(i));
            }
            // If only the before node is available
            else {
                for (int i = 0; i < parent.returnChildren().size(); i++)
                    left.appendChild(parent.returnChild(i));
            }
            // Delete the parent node after merging
            copy = parent.returnParent();
            parent.removeNode();
            deletedCount++;
        }
        // If borrowing keys is possible
        else {
            if (left != null && right != null) {
                // Take keys from the end of the before node that can be spared
                for (int i = 0; i < leftSpare && i < needed; i++) {
                    
                    parent.appendChild(left.returnChild(left.returnChildren().size() - 1), 0);
                    left.removeChild(left.returnChild(left.returnChildren().size() - 1));
                }
                // Take the remaining keys from the beginning of the after node
                for (int i = leftSpare; i < needed; i++) {
                    parent.appendChild(right.returnChild(0));
                    right.removeChild(right.returnChild(0));
                }
            } else if (left == null) {
                // Take all keys from the after node if there's no before node
                for (int i = 0; i < needed; i++) {
                    parent.appendChild(right.returnChild(0));
                    right.removeChild(right.returnChild(0));
                }
            } else {
                // Take all keys from the before node if there's no after node
                for (int i = 0; i < needed; i++) {
                    parent.appendChild(left.returnChild(left.returnChildren().size() - 1 - i), 0);
                    left.removeChild(left.returnChild(left.returnChildren().size() - 1 - i));
                }
            }
            copy = parent.returnParent();
        }
        // Recursively update the parent nodes
        resetParent(copy);
    }

    /**
     * Retrieves records associated with the given key.
     * 
     * @param key The key to search for.
     * @return An ArrayList containing the addresses associated with the given key.
     */
    public ArrayList<Address> getRecordsWithKey(int key) {
        ArrayList<Address> result = new ArrayList<>();
        Node currentNode = root;
        ParentNode parentNode;

        // Searching for leaf node with the key
        while (!currentNode.returnLeaf()) {
            parentNode = (ParentNode) currentNode;
            for (int i = 0; i < parentNode.returnKeys().size(); i++) {
                if (key <= parentNode.returnKey(i)) {
                    currentNode = parentNode.returnChild(i);
                    break;
                }
                if (i == parentNode.returnKeys().size() - 1) {
                    currentNode = parentNode.returnChild(i+1);
                    break;
                }
            }
        }

        // Finding records with the same key within the leaf node
        LeafNode currentLeaf = (LeafNode) currentNode;
        boolean done = false;
        while (!done && currentLeaf != null) {
            for (int i = 0; i < currentLeaf.returnKeys().size(); i++) {
                if (currentLeaf.returnKey(i) == key) {
                    result.add(currentLeaf.returnRecord(i));
                    continue;
                }
                if (currentLeaf.returnKey(i) > key) {
                    done = true;
                    break;
                }
            }
            if (!done) {
                if (currentLeaf.returnNext() != null) {
                    currentLeaf = currentLeaf.returnNext();
                } else {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Retrieves records associated with keys within the given range.
     * 
     * @param min The minimum value of the range.
     * @param max The maximum value of the range.
     * @return An ArrayList containing the addresses associated with keys within the
     *         specified range.
     */
    public ArrayList<Address> getRecordsWithKeyInRange(int min, int max) {
        ArrayList<Address> result = new ArrayList<>();
        int nodeAccess = 1; // Access the root
        Node curNode = root;
        ParentNode parentNode;

        // Searching for the leaf node with a key within the range
        while (!curNode.returnLeaf()) {
            parentNode = (ParentNode) curNode;
            for (int i = 0; i < parentNode.returnKeys().size(); i++) {
                if (min <= parentNode.returnKey(i)) {
                    curNode = parentNode.returnChild(i);
                    nodeAccess++;
                    break;
                }
                if (i == parentNode.returnKeys().size() - 1) {
                    curNode = parentNode.returnChild(i + 1);
                    nodeAccess++;
                    break;
                }
            }
        }

        // Finding records with keys within the range within the leaf node
       LeafNode curLeaf = (LeafNode) curNode;
        boolean done = false;
        while (!done && curLeaf != null) {
            for (int i = 0; i < curLeaf.returnKeys().size(); i++) {
                if (curLeaf.returnKey(i) >= min && curLeaf.returnKey(i) <= max) {
                    result.add(curLeaf.returnRecord(i));
                    continue;
                }
                if (curLeaf.returnKey(i) > max) {
                    done = true;
                    break;
                }
            }
            if (!done) {
                if (curLeaf.returnNext() != null) {
                    curLeaf = (LeafNode) curLeaf.returnNext();
                    nodeAccess++;
                } else {
                    break;
                }
            }
        }
        System.out.println("B+ Tree Search in Range: "+ String.format("%d records found with %d index nodes accessed", result.size(), nodeAccess));
        return result;
    }

    /**
     * Prints statistics of the B+ tree.
     */
    public void bPlusTreeStats() {
        ArrayList<Integer> rootKeys = new ArrayList<>();
        ArrayList<Integer> firstKeys = new ArrayList<>();
        ParentNode rootCopy = (ParentNode) root;
        Node first = rootCopy.returnChild(0);

        for (int i = 0; i < root.returnKeys().size(); i++) {
            rootKeys.add(root.returnKey(i));
        }

        for (int i = 0; i < first.returnKeys().size(); i++) {
            firstKeys.add(first.returnKey(i));
        }
        System.out.println("B+ Tree Statistics");
        System.out.println("n: " + maxKeys);
        System.out.println("height: " + height);
        System.out.println("number of nodes: " + nodeCount);
        System.out.println("Content of root node: " + rootKeys);
        System.out.println("Content of first child: " + firstKeys);
    }
}
