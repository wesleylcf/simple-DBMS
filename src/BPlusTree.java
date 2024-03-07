import java.util.ArrayList;

public class BPlusTree {
    private static final int POINTER_SIZE = 8;
    private static final int KEY_SIZE = 4;

    int maxKeys;
    int parentMinKeys;
    int leafMinKeys;
    int height;
    int nodeCount;
    int deletedCount;
    Node root;

    public BPlusTree(int blockSize) {
        maxKeys = (blockSize - POINTER_SIZE) / (KEY_SIZE + POINTER_SIZE);
        parentMinKeys = (int) Math.floor(maxKeys / 2);
        leafMinKeys = (int) Math.floor((maxKeys + 1) / 2);
        root = createTree();
        nodeCount = 0;
        deletedCount = 0;
        System.out.println("B+ tree initialized with maxKeys = " + maxKeys + ", parentMinKeys = " + parentMinKeys + ", leafMinKeys = " + leafMinKeys);
    }

    public Node createTree() {
        Node root = new Node(false, false);
        root.setRoot(true);
        height = 1;
        nodeCount = 1;
        return root;
    }

    public void insert(int key, Address address) {

        Node leafNode = new Node(false, true);
        leafNode = this.searchLeaf(key);

        if (leafNode.returnKeys().size() < maxKeys) leafNode.addRecord(key, address);

        else {

            splitLeaf(leafNode, key, address);
        }

    }

    // search for the correct leaf for record insertion
    public Node searchLeaf(int key) {
        if (this.root.returnLeaf()) return (Node) root;

        Node parentNode = new Node(true, false);
        ArrayList<Integer> keys;

        // finding correct first level parent
        while (!parentNode.returnChild(0).returnLeaf()) {
            keys = parentNode.returnKeys();

            for (int i = keys.size() - 1; i >= 0; i--) {
                if (keys.get(i) <= key) {
                    parentNode = parentNode.returnChild(i + 1);
                    break;
                } else if (i == 0) parentNode = parentNode.returnChild(0);
            }
        }

        // finding correct leaf
        keys = parentNode.returnKeys();
        for (int i = keys.size() - 1; i >= 0; i--) {

            if (keys.get(i) <= key)
                return parentNode.returnChild(i + 1);
        }
        return parentNode.returnChild(0);
    }

    public void splitLeaf(Node originalNode, int key, Address address) {
        int keys[] = new int[maxKeys + 1];
        Address addresses[] = new Address[maxKeys + 1];
        Node newLeaf = new Node(false, true);
        int i;

        // getting full and sorted lists of keys and addresses
        for (i = 0; i < maxKeys; i++) {

            keys[i] = originalNode.returnKey(i);
            addresses[i] = originalNode.returnRecord(i);
        }

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

        // clearing old leafnode values
        originalNode.deleteKeys();
        originalNode.deleteRecords();

        // putting the keys and addresses into the two leafnodes
        for (i = 0; i < leafMinKeys; i++)
            originalNode.addRecord(keys[i], addresses[i]);

        for (i = leafMinKeys; i < maxKeys + 1; i++)
            newLeaf.addRecord(keys[i], addresses[i]);

        // setting old leafnode to point to new leafnode and new leafnode to point to
        // next leafnode
        newLeaf.setNext(originalNode.returnNext());
        originalNode.setNext(newLeaf);

        // setting parents for new leafnode
        if (originalNode.returnRoot()) {

            Node newRoot = new Node(true, false);
            originalNode.setRoot(false);
            newRoot.setRoot(true);
            newRoot.appendChild(originalNode);
            newRoot.appendChild(newLeaf);
            root = newRoot;
            height++;
        } else if (originalNode.returnParent().returnKeys().size() < maxKeys)
            originalNode.returnParent().appendChild(newLeaf);
        else
            splitParent(originalNode.returnParent(), newLeaf);

        // updating nodeCount
        nodeCount++;
    }

    public void splitParent(Node parentNode, Node childNode) {


        Node children[] = new Node[maxKeys + 2];
        int keys[] = new int[maxKeys + 2];
        int key = childNode.returnSmallest();
        Node newParentNode = new Node(true, false);

        // getting full and sorted lists of keys and children
        for (int i = 0; i < maxKeys + 1; i++) {
            children[i] = parentNode.returnChild(i);
            keys[i] = children[i].returnSmallest();
        }

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

        //clearing old parent values
        parentNode.deleteKeys();
        parentNode.deleteRecords();

        // putting the children into the two parentnodes
        for (int i = 0; i < parentMinKeys + 2; i++)
            parentNode.appendChild(children[i]);

        for (int i = parentMinKeys + 2; i < maxKeys + 2; i++)
            parentNode.appendChild(children[i]);

        //setting parent for the new parentnode
        if (parentNode.returnRoot()) {

            Node newRoot = new Node(true, false);
            parentNode.setRoot(false);
            newRoot.setRoot(true);
            newRoot.appendChild(parentNode);
            newRoot.appendChild(newParentNode);
            root = newRoot;
            height++;
        } else if (parentNode.returnParent().returnKeys().size() < maxKeys)
            parentNode.returnParent().appendChild(newParentNode);
        else
            splitParent(parentNode.returnParent(), newParentNode);

        // updating nodeCount
        nodeCount++;

    }

    public void deleteKey(int key) {

        ArrayList<Integer> keys;
        Node leaf;

        // while there are still records with given key value
        while (getRecordsWithKey(key, false).size() != 0) {

            leaf = searchLeaf(key);
            keys = leaf.returnKeys();

            // delete one record and update tree 
            for (int i = 0; i < keys.size(); i++) {

                if (keys.get(i) == key) {

                    leaf.deleteRecord(i);

                    // if leafnode is not root then update tree
                    if (!leaf.returnRoot())
                        resetLeaf(leaf);

                    break;
                }
            }
        }

        System.out.println("deletion: number of nodes deleted = " + deletedCount);
        nodeCount -= deletedCount;
        treeStats();
    }

    public void resetLeaf(Node node) {

        // if no need to change node, reset parent and finish
        if (node.returnKeys().size() >= leafMinKeys) {

            resetParent(node.returnParent());
            return;
        }

        Node before = (Node) node.returnParent().returnChildBefore(node);
        Node after = (Node) node.returnParent().returnChildAfter(node);
        int needed = leafMinKeys - node.returnKeys().size();
        int bSpare = 0;
        int aSpare = 0;
        Node copy;

        // getting number of keys that before and after nodes can spare
        if (before != null)
            bSpare += before.returnKeys().size() - leafMinKeys;

        if (after != null)
            aSpare += after.returnKeys().size() - leafMinKeys;

        // if need to merge
        if (needed > aSpare + bSpare) {

            // if node has both before and after nodes
            if (before != null && after != null) {

                // insert as many records as possible into before node
                for (int i = 0; i < maxKeys - (bSpare + leafMinKeys); i++)
                    before.addRecord(node.returnKey(i), node.returnRecord(i));

                // insert the rest into after node
                for (int i = maxKeys - (bSpare + leafMinKeys); i < node.returnKeys().size(); i++)
                    after.addRecord(node.returnKey(i), node.returnRecord(i));
            }

            // if node only has after node
            else if (before == null) {

                for (int i = 0; i < node.returnKeys().size(); i++)
                    after.addRecord(node.returnKey(i), node.returnRecord(i));
            }

            // if node only has before node
            else {

                for (int i = 0; i < node.returnKeys().size(); i++)
                    before.addRecord(node.returnKey(i), node.returnRecord(i));
            }

            // have to copy parent to reset after deleting leafnode
            copy = node.returnParent();

            // have to look for before node if it is not from the same parent
            if (before == null) {

                if (!copy.returnRoot())
                    before = searchLeaf(copy.returnSmallest() - 1);
            }

            // change before to point to after
            before.setNext(node.returnNext());

            // delete node
            node.removeNode();
            deletedCount++;
        }

        // if able to borrow keys
        else {

            if (before != null && after != null) {

                // take the last few keys from before node that can be spared
                for (int i = 0; i < bSpare; i++) {

                    node.addRecord(before.returnKey(before.returnKeys().size() - 1 - i), before.returnRecord(before.returnKeys().size() - 1 - i));
                    before.deleteRecord(before.returnKeys().size() - 1 - i);
                }

                // take the rest from after node
                for (int i = bSpare, j = 0; i < needed; i++, j++) {

                    node.addRecord(after.returnKey(j), after.returnRecord(j));
                    after.deleteRecord(j);
                }
            } else if (before == null) {

                // take all from after node
                for (int i = 0; i < needed; i++) {

                    node.addRecord(after.returnKey(i), after.returnRecord(i));
                    after.deleteRecord(i);
                }
            } else {

                // take all from before node
                for (int i = 0; i < needed; i++) {

                    node.addRecord(before.returnKey(before.returnKeys().size() - 1 - i), before.returnRecord(before.returnKeys().size() - 1 - i));
                    before.deleteRecord(before.returnKeys().size() - 1 - i);
                }
            }

            copy = node.returnParent();
        }

        // update parents
        resetParent(copy);
    }

    public void resetParent(Node parent) {

        // if node is root
        if (parent.returnRoot()) {

            // if root has at least 2 children, reset and return
            if (parent.returnChildren().size() > 1) {

                // lazy man's reset
                Node child = parent.returnChild(0);
                parent.removeChild(child);
                parent.appendChild(child);
                return;
            }

            // if root has 1 child, delete root level
            else {

                parent.returnChild(0).setRoot(true);
                root = parent.returnChild(0);
                parent.removeNode();
                deletedCount++;
                height--;
                return;
            }
        }

        Node before = (Node) parent.returnParent().returnChildBefore(parent);
        Node after = (Node) parent.returnParent().returnChildAfter(parent);
        int needed = parentMinKeys - parent.returnKeys().size();
        int bSpare = 0;
        int aSpare = 0;
        Node copy;

        if (before != null)
            bSpare += before.returnKeys().size() - parentMinKeys;

        if (after != null)
            aSpare += after.returnKeys().size() - parentMinKeys;

        // if need to merge
        if (needed > aSpare + bSpare) {

            // if node has both before and after nodes
            if (before != null && after != null) {

                // insert as many records as possible into before node
                for (int i = 0; i < maxKeys - (bSpare + parentMinKeys) + 1 && i < parent.returnChildren().size(); i++)
                    before.appendChild(parent.returnChild(i));

                // insert the rest into after node
                for (int i = maxKeys - (bSpare + parentMinKeys) + 1; i < parent.returnChildren().size(); i++)
                    after.appendChild(parent.returnChild(i));
            }

            // if node only has after node
            else if (before == null) {

                for (int i = 0; i < parent.returnChildren().size(); i++)
                    after.appendChild(parent.returnChild(i));
            }

            // if node only has before node
            else {

                for (int i = 0; i < parent.returnChildren().size(); i++)
                    before.appendChild(parent.returnChild(i));
            }

            // delete after merging
            copy = parent.returnParent();
            parent.removeNode();
            deletedCount++;
        }

        // if able to borrow keys
        else {

            if (before != null && after != null) {

                // take the last few keys from before node that can be spared
                for (int i = 0; i < bSpare && i < needed; i++) {

                    parent.appendChild(before.returnChild(before.returnChildren().size() - 1), 0);
                    before.removeChild(before.returnChild(before.returnChildren().size() - 1));
                }

                // take the rest from after node
                for (int i = bSpare; i < needed; i++) {

                    parent.appendChild(after.returnChild(0));
                    after.removeChild(after.returnChild(0));
                }
            } else if (before == null) {

                // take all from after node
                for (int i = 0; i < needed; i++) {

                    parent.appendChild(after.returnChild(0));
                    after.removeChild(after.returnChild(0));
                }
            } else {

                // take all from before node
                for (int i = 0; i < needed; i++) {

                    parent.appendChild(before.returnChild(before.returnChildren().size() - 1 - i), 0);
                    before.removeChild(before.returnChild(before.returnChildren().size() - 1 - i));
                }
            }

            copy = parent.returnParent();
        }

        resetParent(copy);
    }

    public ArrayList<Address> getRecordsWithKey(int key) {
        return getRecordsWithKey(key, true);
    }

    public ArrayList<Address> getRecordsWithKey(int key, boolean isVerbose) {
        ArrayList<Address> result = new ArrayList<>();
        int blockAccess = 1; // access the root??
        int siblingAccess = 0;
        if (isVerbose)
            System.out.println("B+Tree.keySearch" + "[Node Access] Access root node");

        Node currentNode = root;
        Node parentNode;
        // searching for leaf node with key
        while (!currentNode.returnLeaf()) {
            parentNode = (Node) currentNode;
            for (int i = 0; i < parentNode.returnKeys().size(); i++) {
                if (key <= parentNode.returnKey(i)) {
                    if (isVerbose) {
                        System.out.println("B+Tree.keySearch" + currentNode.toString());
                        System.out.println(String.format("B+Tree.keySearch [Node Access] follow pointer [%d]: key(%d)<=curKey(%d)", i, key, parentNode.returnKey(i)));
                    }
                    currentNode = parentNode.returnChild(i);
                    blockAccess++;
                    break;
                }
                if (i == parentNode.returnKeys().size() - 1) {
                    if (isVerbose) {
                        System.out.println("B+Tree.keySearch" + currentNode.toString());
                        System.out.println(String.format("B+Tree.keySearch [Node Access] follow pointer [%d+1]: last key and key(%d)>curKey(%d)", i, key, parentNode.returnKey(i)));
                    }
                    currentNode = parentNode.returnChild(i + 1);
                    blockAccess++;
                    break;
                }
            }
        }
        // after leaf node is found, find all records with same key
        Node currentLeaf = (Node) currentNode;
        boolean done = false;
        while (!done && currentLeaf != null) {
            // finding same keys within leaf node
            for (int i = 0; i < currentLeaf.returnKeys().size(); i++) {
                // found same key, add into result list
                if (currentLeaf.returnKey(i) == key) {
                    result.add(currentLeaf.returnRecord(i));
                    continue;
                }
                // if curKey > searching key, no need to continue searching
                if (currentLeaf.returnKey(i) > key) {
                    done = true;
                    break;
                }
            }
            if (!done) {
                // trying to check sibling node has remaining records of same key
                if (currentLeaf.returnNext() != null) {
                    currentLeaf = currentLeaf.returnNext();
                    blockAccess++;
                    siblingAccess++;
                } else {
                    break;
                }
            }
        }

        if (siblingAccess > 0) {
            if (isVerbose) {
                System.out.println("B+Tree.keySearch" + "[Node Access] " + siblingAccess + " sibling node access");
            }
        }
        if (isVerbose) {
            System.out.println(String.format("B+Tree.keySearch input(%d): %d records found with %d node access", key, result.size(), blockAccess));
        }
        return result;
    }

    public void treeStats() {

        ArrayList<Integer> rootKeys = new ArrayList<Integer>();
        ArrayList<Integer> firstKeys = new ArrayList<Integer>();
        Node rootCopy = (Node) root;
        Node first = rootCopy.returnChild(0);

        for (int i = 0; i < root.returnKeys().size(); i++) {

            rootKeys.add(root.returnKey(i));
        }

        for (int i = 0; i < first.returnKeys().size(); i++) {

            firstKeys.add(first.returnKey(i));
        }

        System.out.println("treeStats: n = " + maxKeys + ", number of nodes = " + nodeCount + ", height = " + height);
        System.out.println("rootContents" + "root node contents = " + rootKeys);
        System.out.println("firstContents" + "first child contents = " + firstKeys);
    }

    public ArrayList<Address> getRecordsWithKeyInRange(int min, int max) {
        return getRecordsWithKeyInRange(min, max, true);
    }

    public ArrayList<Address> getRecordsWithKeyInRange(int min, int max, boolean isVerbose) {
        ArrayList<Address> result = new ArrayList<>();
        int nodeAccess = 1; // access the root
        int siblingAccess = 0;
        if (isVerbose) {
            System.out.println("B+Tree.rangeSearch [Node Access] Access root node");
        }
        Node curNode = root;
        Node parentNode;
        // searching for leaf node with key
        while (!curNode.returnLeaf()) {
            parentNode = (Node) curNode;
            for (int i = 0; i < parentNode.returnKeys().size(); i++) {
                if (min <= parentNode.returnKey(i)) {
                    if (isVerbose) {
                        System.out.println("B+Tree.rangeSearch" + curNode.toString());
                        System.out.println("B+Tree.rangeSearch " + String.format("[Node Access] follow pointer [%d]: min(%d)<=curKey(%d)", i, min, parentNode.returnKey(i)));
                    }
                    curNode = parentNode.returnChild(i);
                    nodeAccess++;
                    break;
                }
                if (i == parentNode.returnKeys().size() - 1) {
                    if (isVerbose) {
                        System.out.println("B+Tree.rangeSearch" + curNode.toString());
                        System.out.println("B+Tree.rangeSearch " + String.format("[Node Access] follow pointer [%d+1]: last key and min(%d)>curKey(%d)", i, min, parentNode.returnKey(i)));
                    }
                    curNode = parentNode.returnChild(i + 1);
                    nodeAccess++;
                    break;
                }
            }
        }
        // after leaf node is found, find all records with same key
        Node curLeaf = (Node) curNode;
        boolean done = false;
        while (!done && curLeaf != null) {
            // finding same keys within leaf node
            for (int i = 0; i < curLeaf.returnKeys().size(); i++) {
                // found same key, add into result list
                if (curLeaf.returnKey(i) >= min && curLeaf.returnKey(i) <= max) {
                    result.add(curLeaf.returnRecord(i));
                    continue;
                }
                // if curKey > searching key, no need to continue searching
                if (curLeaf.returnKey(i) > max) {
                    done = true;
                    break;
                }
            }
            if (!done) {
                // trying to check sibling node has remaining records of same key
                if (curLeaf.returnNext() != null) {
                    curLeaf = (Node) curLeaf.returnNext();
                    nodeAccess++;
                    siblingAccess++;
                } else {
                    break;
                }
            }
        }
        if (siblingAccess > 0) {
            if (isVerbose) {
                System.out.println("B+Tree.rangeSearch" + "[Node Access] " + siblingAccess + " sibling node access");
            }
        }
        if (isVerbose) {
            System.out.println("B+Tree.rangeSearch " + String.format("input(%d, %d): %d records found with %d node access", min, max, result.size(), nodeAccess));
        }
        return result;
    }


}
