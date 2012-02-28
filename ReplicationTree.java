import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

public class ReplicationTree extends JFrame {
    public static void main(String[] args) throws InterruptedException {
        new ReplicationTree();
    }

    public ReplicationTree() throws InterruptedException {
        super("Creating a Simple JTree");
        // WindowUtilities.setNativeLookAndFeel();
        // addWindowListener(new ExitListener());
        Container content = getContentPane();

        ValueReplicationTree vpTree = new ValueReplicationTree(6);
        DefaultMutableTreeNode root = vpTree.getTop();
        JTree tree = new JTree(root);
        content.add(new JScrollPane(tree), BorderLayout.CENTER);
        setSize(275, 300);
        setVisible(true);

        vpTree.simulation(4);

        System.out.println("search cost is " + vpTree.searchCounter);
        System.out.println("update cost is " + vpTree.updateCounter);
        System.out.println("program finished.");
    }
}

class TreeStructure {
    private int targetHeight;
    private DefaultMutableTreeNode top;

    public TreeStructure(int h) {
        targetHeight = h;

        // create the root node
        top = new DefaultMutableTreeNode();

        System.out.println("created the tree");

        // recursively build the tree from the root
        createChildrenFor(top);
    }

    // recursively populates the subtree starting at this node,
    // with each node having a random number of children between 1 and 3,
    // until targetHieght number of levels have been built
    public void createChildrenFor(DefaultMutableTreeNode node) {
        long numOfChildren = 1 + Math.round(Math.random());

        for (int i = 0; i < numOfChildren; ++i) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();
            node.add(newNode);
        }

        if (node.getLevel() < (targetHeight - 1))
            for (int n = 0; n < node.getChildCount(); ++n)
                createChildrenFor((DefaultMutableTreeNode) node.getChildAt(n));
    }

    // prints out the hierarchy of the tree on the screen
    public void printTreeStartingAt(DefaultMutableTreeNode node) {
        Enumeration e = node.breadthFirstEnumeration();
        for (; e.hasMoreElements();)
            System.out.println(e.nextElement());
    }

    public DefaultMutableTreeNode getTop() {
        return top;
    }
}

class ValueReplicationTree extends TreeStructure {
    public ValueReplicationTree(int w) {
        super(w);
        fillLeaves();
        fillInternalNodes();
        callsByNumber = new HashMap();
        replicaTally = new HashMap();
        // intialize the tallys
        for (int i = 0; i < MAX_NUMBER_OF_PHONES; i++) {
            moveTally[i] = 1; //consider the initial deployment the first move
        }
        searchCounter = 0;
        updateCounter=0;
       
    }

    // this will give each leaf node in the tree a list of phone numbers
    public void fillLeaves() {
        int j = 0;
        Enumeration e = getTop().breadthFirstEnumeration();

        // find leaves and insert some numbers to each one
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
                    .nextElement();
            if (node.isLeaf()) {
                long numberCount = 1 + Math.round(Math.random());
                ArrayList phoneNumbers = new ArrayList();

                for (int i = 0; i < numberCount; ++i, ++j)
                    phoneNumbers.add(j);
                node.setUserObject(phoneNumbers);
            }
        }
    }

    // this will fill each internal node with a map of the phone numbers
    // in its subtree the cell they are currently in
    public void fillInternalNodes() {
        Enumeration e = getTop().breadthFirstEnumeration();

        // find internal nodes
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
                    .nextElement();
            if (!node.isLeaf()) {
                HashMap db = new HashMap();

                // create the subtree at this node and find all leaf nodes in it
                Enumeration f = node.breadthFirstEnumeration();
                while (f.hasMoreElements()) {
                    DefaultMutableTreeNode otherNode = (DefaultMutableTreeNode) f
                            .nextElement();
                    if (otherNode.isLeaf()) {
                        // get the phone numbers from this leaf and add them to
                        // the hashMap
                        ArrayList listOfNumbers = (ArrayList) otherNode
                                .getUserObject();

                        for (int i = 0; i < listOfNumbers.size(); ++i)
                            db.put(listOfNumbers.get(i), otherNode);
                    }
                }

                node.setUserObject(db);
            }
        }
    }

    // given a starting leaf node, will find the node containing the phone
    // number in question
    public DefaultMutableTreeNode call(DefaultMutableTreeNode startingNode,
            int phoneNumber) {
       
        if(startingNode==null) {
            System.out.println("calling number doesn't exit.");
            return null;
        }
        System.out.println("a phone in " + startingNode + " wants to call "
                + phoneNumber);
        //System.out.println("starting call at: " + startingNode);

        // first search the same cell, if found then just return the local cell
        ArrayList localNumbers = (ArrayList) startingNode.getUserObject();
        if (localNumbers.contains(phoneNumber))
            return startingNode;

        DefaultMutableTreeNode currentNode = startingNode;
        HashMap map;

        // not found locally, start moving up the tree until an entry
        // for the requested number is found or we get to the root
        do {
            currentNode = (DefaultMutableTreeNode) currentNode.getParent();
            map = (HashMap) currentNode.getUserObject();
            //System.out.println("moving up to " + currentNode);
            searchCounter++;
        } while (!map.containsKey(phoneNumber) && currentNode != getTop());

        // if the number is still not found, we assume this means we hit
        // the top of the tree and return failure
        if (!map.containsKey(phoneNumber)) {
            System.out.println("The number you are trying to reach does not exist.");
            return null;
        } else {
            // if we are here, it means we have found the number and can
            // just return the location (node) mapped to that phone number
            map = (HashMap) currentNode.getUserObject();
            DefaultMutableTreeNode found = currentNode;
            currentNode = (DefaultMutableTreeNode) map.get(phoneNumber);
            //System.out.println("moving down to " + currentNode);
       
            // just to make sure, check the resulting node to see if it
            // has the phone number we want (should always happen unless
            // something outside this object modified the tree)
            localNumbers = (ArrayList) currentNode.getUserObject();
            if (localNumbers.contains(phoneNumber)) {
                // check if replication is needed
                replication(startingNode, found, phoneNumber, currentNode);
                return currentNode;
            }
        }

        // this is just because java wants a return statement at the
        // end of a function
        return null;
    }

    public void replication(DefaultMutableTreeNode callerCell,
            DefaultMutableTreeNode numberFoundAt, int receiverNumber,
            DefaultMutableTreeNode receiverCell) {
        System.out.println("Replication function called");
        // cellTotal is total number of calls to a particular number from an
        // internal node. Higher level internal nodes' cellTotal
        // is the sum of its children
        int cellTotal = 0;
        ArrayList replicaSites;
       
        // first calculate how the most recent call changes LCMR

        // nobody called this number yet
        if (callsByNumber.get(receiverNumber) == null) {
            // mapping all nodes to their total calls to a particular number
            HashMap callsByNode = new HashMap();

            // create entries for all nodes from callerCell to the node where
            // the number is already stored
            DefaultMutableTreeNode currentNode = callerCell;

            while (currentNode != numberFoundAt) {
                //System.out.println("updating total number of calls to "
                    //    + receiverNumber + " from " + currentNode);
                callsByNode.put(currentNode, 1);
                currentNode = (DefaultMutableTreeNode) currentNode.getParent();
                updateCounter++;
            }
            callsByNumber.put(receiverNumber, callsByNode);
        } else {// calls already made to this number
            HashMap callsByNode = (HashMap) callsByNumber.get(receiverNumber);
            DefaultMutableTreeNode currentNode = callerCell;
            while (currentNode != numberFoundAt) {
                //System.out.println("updating total calls to " + receiverNumber
                    //    + " from " + currentNode);
                if (callsByNode.get(currentNode) == null)
                    callsByNode.put(currentNode, 1);
                else {
                    cellTotal = (Integer) callsByNode.get(currentNode);
                    cellTotal++;
                    callsByNode.put(currentNode, cellTotal);
                    callsByNumber.put(receiverNumber, callsByNode);

                    // calculate LCMR and decide where, if any, to replicate
                    if (!(currentNode.isLeaf())) { // don't replicate at
                        // leaf
                        if ((cellTotal / moveTally[receiverNumber]) > S_MAX) {
                            System.out.println("LCMR is bigger than S_MAX");
                            // replicate receiverNumber at the current node

                            System.out.println("replicating " + receiverNumber
                                    + " at " + currentNode);

                            HashMap map = (HashMap) currentNode.getUserObject();
                            map.put(receiverNumber, receiverCell);
                            //update replicaTally
                           
                            if(replicaTally.get(receiverNumber) == null) //this is the first time replica is created
                                replicaSites = new ArrayList();
                            else
                                replicaSites = (ArrayList) replicaTally.get(receiverNumber);
                            replicaSites.add(currentNode);
                            replicaTally.put(receiverNumber, replicaSites);
                        } else if ((cellTotal / moveTally[receiverNumber]) > S_MIN) {
                            // check node level, which is the number of levels
                            // above the current node
                            // and check how many times this number has been
                            // replicated
                           
                            System.out.println("LCMR is in between S_MAX and S_MIN with " + receiverNumber
                                    + " at " + currentNode);
                            if (currentNode.getLevel() > L){
                                System.out.println("The current node is " + currentNode.getLevel() + " levels from the top.");
                                           
                                if(replicaTally.get(receiverNumber) == null) //this is the first time replica is created
                                    replicaSites = new ArrayList();
                                else
                                    replicaSites = (ArrayList) replicaTally.get(receiverNumber);
                                if ((replicaSites.size() < N_MAX))  {
                                    // replicate   
                                    System.out.println(receiverNumber + " has been replicated " + replicaSites.size()+ " times before.");
                                    System.out.println("replicating " + receiverNumber
                                            + " at " + currentNode);
                                    HashMap map = (HashMap) currentNode
                                            .getUserObject();
                                    map.put(receiverNumber, receiverCell);
                                    replicaSites.add(currentNode);
                                    replicaTally.put(receiverNumber, replicaSites);
                               
                                }
                            }
                        }
                    }
                }
                currentNode = (DefaultMutableTreeNode) currentNode.getParent();
                updateCounter++;
            }// end of making replication, if warranted, on each relevant node
        }
    }

    // this will move a phone number from its current cell to
    // a given destination cell
    public boolean move(int phoneNumber, DefaultMutableTreeNode source,
            DefaultMutableTreeNode dest) {
       
        if(source==null) {
            System.out.println("Move's source location doesn't exit.");
            return false;
        }
        if(dest==null) {
            System.out.println("Move's Destination location doesn't exist.");
            return false;
        }
       
       
        ArrayList list;
        HashMap map;
        ArrayList replicaSites;

        // check that the number is in source. If it is not
        // in source, do not proceed with the move
        list = (ArrayList) source.getUserObject();
        if (!list.contains(phoneNumber))
            return false;

        // if source and dest are the same node, no need to move
        // (considerred a successful move)
        if (source == dest)
            return true;

        // delete all entries for this phone number
        // between source and root

        DefaultMutableTreeNode currentNode;
        TreeNode[] path = source.getPath();

        for (int i = 0; i < path.length; ++i) {
            currentNode = (DefaultMutableTreeNode) path[i];
        //    System.out.println("removing " + phoneNumber + " from " + currentNode);

            if (currentNode.isLeaf()) {
                list = (ArrayList) currentNode.getUserObject();
                list.remove(list.indexOf(phoneNumber));
            } else {
                map = (HashMap) currentNode.getUserObject();
                map.remove(phoneNumber);
                updateCounter++;
            }
        }

        // add entries between dest and root
        path = dest.getPath();

        for (int i = 0; i < path.length; ++i) {
            currentNode = (DefaultMutableTreeNode) path[i];
        //    System.out.println("adding " + phoneNumber + " to " + currentNode);

            if (currentNode.isLeaf()) {
                list = (ArrayList) currentNode.getUserObject();
                list.add(phoneNumber);
            } else {
                // update this node's local map so the phone number
                // maps to its current location (dest)
                map = (HashMap) currentNode.getUserObject();
                map.put(phoneNumber, dest);
                updateCounter++;
            }
        }
       
        System.out.println(phoneNumber + " has moved from " + source + " to " + dest);
        moveTally[phoneNumber]++;
        System.out.println(phoneNumber + " has moved " + (moveTally[phoneNumber]-1) + " times");
        //change all replication sites's data to reflect this change
        if(replicaTally.get(phoneNumber)!=null){
            replicaSites = (ArrayList)replicaTally.get(phoneNumber);
            for (int i=0; i<replicaSites.size(); i++){
                DefaultMutableTreeNode node =(DefaultMutableTreeNode)replicaSites.get(i);
                HashMap localMap = (HashMap)node.getUserObject();
                localMap.remove(phoneNumber);
                localMap.put(phoneNumber, dest);   
                System.out.println("Update " + phoneNumber +"'s replication at " + node);
        }
           
        }
       

        return true;
    }

    // this is for testing
    public DefaultMutableTreeNode find(int phoneNumber) {
        DefaultMutableTreeNode root = getTop();
        HashMap map = (HashMap) root.getUserObject();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) map
                .get(phoneNumber);
        return node;
    }

    public void simulation(int times) {
        //find largest phone number for use of simulation
        DefaultMutableTreeNode root = getTop();
        DefaultMutableTreeNode lastleaf = root.getLastLeaf();
        ArrayList numbers = (ArrayList)lastleaf.getUserObject();
        int largestNumber = 0;
        for (int i= 0; i<numbers.size(); i++)
            largestNumber = (Integer)numbers.get(i);

        //for simulating random number of calls
        for (int i=0; i < times; i++){
            if (Math.random()>=0.5) {
                int calling = (int)Math.round((Math.random()*largestNumber));
                int receiving = (int)Math.round((Math.random()*largestNumber));
                call(find(calling), receiving);
            }
            else {
                int moving = (int)Math.round((Math.random()*largestNumber));
                int joining = (int)Math.round((Math.random()*largestNumber));
                move(moving, find(moving), find(joining));
            }
        }
}
    private static final int MAX_NUMBER_OF_PHONES = 1000;
    private static final int S_MIN = 2; // LCMR below this no replication will
    // carry out
    private static final int S_MAX = 5; // LCMR higer than this will trigger
    // replication
    private static final int N_MAX = 20; // maximum number of replicas per phone number
    private static final int L = 2; // maximum level of hierarchy replicas can be
    // placed
    // mapping all phone numbers to a corresponding callsByNode, which itself is
    // a map
    private HashMap callsByNumber;
   
    // keep track of how many replicas a number has
    private HashMap replicaTally;
    // keep track of moves by a particular phone
    private int moveTally[] = new int[MAX_NUMBER_OF_PHONES];
   
    //the counters are for cost comparisons
    int searchCounter;
    int updateCounter;
}