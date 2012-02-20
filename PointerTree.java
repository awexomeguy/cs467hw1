import java.util.*;
import javax.swing.tree.*;

public class PointerTree extends TreeStructure
{
	public PointerTree(int h, int w)
	{
		super(h, w);
		fillLeaves();
		fillInternalNodes();
	}	
	
	// this will give each leaf node in the tree a list of phone numbers
	public void fillLeaves()
	{
		int j = 0;
		Enumeration e = getTop().breadthFirstEnumeration();
		
		// find leaves and insert some numbers to each one
		while(e.hasMoreElements())
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
			if(node.isLeaf())
			{
				ArrayList phoneNumbers = new ArrayList();
				for(int i = 0; i < 3; ++i, ++j)
					phoneNumbers.add(j);
				
				node.setUserObject(phoneNumbers);
				System.out.println("found a leaf");
			}
		}
	}
	
	// this will fill each internal node with a map of the phone numbers
	// in its subtree to a pointer which will eventually locate the cell
	// containing the phone number
	public void fillInternalNodes()
	{
		Enumeration e = getTop().breadthFirstEnumeration();
		
		// find internal nodes
		while(e.hasMoreElements())
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
			if(!node.isLeaf())
			{
				HashMap db = new HashMap();
			
				// create the subtree at this node and find all leaf nodes in it
				Enumeration f = node.breadthFirstEnumeration();
				while(f.hasMoreElements())
				{
					DefaultMutableTreeNode otherNode = (DefaultMutableTreeNode)f.nextElement();
					if(otherNode.isLeaf())
					{
						// get the phone numbers from this leaf and add them to the hashMap
						ArrayList listOfNumbers = (ArrayList)otherNode.getUserObject();
						while(otherNode.getParent() != node)
							otherNode = (DefaultMutableTreeNode)otherNode.getParent();
						
						int pointer = node.getIndex(otherNode);
						for(int i = 0; i < listOfNumbers.size(); ++i)
							db.put(listOfNumbers.get(i), pointer);
					}
				}
				
				node.setUserObject(db);
			}
		}
	}
	
	// given a starting leaf node, will find the node containing the phone
	// number in question
	public DefaultMutableTreeNode call(DefaultMutableTreeNode startingNode, int phoneNumber)
	{
		System.out.println("starting call at: " + startingNode);
		// first search the same cell, if found then just return the local cell
		ArrayList localNumbers = (ArrayList)startingNode.getUserObject();
		if(localNumbers.contains(phoneNumber))
			return startingNode;
		
		DefaultMutableTreeNode currentNode = startingNode;
		HashMap map;
		
		// not found locally, start moving up the tree until an entry
		// for the requested number is found or we get to the root
		do
		{
			currentNode = (DefaultMutableTreeNode)currentNode.getParent();
			map = (HashMap)currentNode.getUserObject();
			System.out.println("moving up to " + currentNode);
		}while(!map.containsKey(phoneNumber) && currentNode != getTop());
		
		// if the number is still not found, we assume this means we hit
		// the top of the tree and return failure
		if(!map.containsKey(phoneNumber))
			return null;
		else
		{
			// if we are here, it means we have found the number and can
			// start following pointers until we get to the correct cell
			while(!currentNode.isLeaf())
			{
				map = (HashMap)currentNode.getUserObject();
				currentNode = (DefaultMutableTreeNode)currentNode.getChildAt((Integer)map.get(phoneNumber));
				System.out.println("moving down to " + currentNode);
			}
			
			// just to make sure, check the resulting node to see if it
			// has the phone number we want (should always happen unless
			// something outside this object modified the tree)
			localNumbers = (ArrayList)currentNode.getUserObject();
			if(localNumbers.contains(phoneNumber))
				return currentNode;
		}
		
		// this is just because java wants a return statement at the
		// end of a function
		return null;
	}
}
