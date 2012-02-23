import java.util.*;
import javax.swing.tree.*;

public class PointerTree extends TreeStructure
{
	public PointerTree(int w)
	{
		super(w);
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
				long numberCount = 1 + Math.round(Math.random() * 4);
				ArrayList phoneNumbers = new ArrayList();
				
				for(int i = 0; i < numberCount; ++i, ++j)
					phoneNumbers.add(j);
				
				node.setUserObject(phoneNumbers);
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
		System.out.println("a phone in " + startingNode + " wants to call " + phoneNumber);
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
	
	// this will move a phone number from its current cell to
	// a given destination cell
	public boolean move(int phoneNumber, DefaultMutableTreeNode source, DefaultMutableTreeNode dest)
	{
		ArrayList list;
		HashMap map;
		
		// check that the number is in source. If it is not
		// in source, do not proceed with the move
		list = (ArrayList)source.getUserObject();
		if(!list.contains(phoneNumber))
			return false;
			
		// if source and dest are the same node, no need to move
		// (considerred a successful move)
		if(source == dest)
			return true;
	
		DefaultMutableTreeNode leastCommonAncestor = (DefaultMutableTreeNode)source.getSharedAncestor(dest);
			
		// delete pointers between source and leastCommonAncestor
		DefaultMutableTreeNode currentNode;
		Enumeration e = source.pathFromAncestorEnumeration(leastCommonAncestor);
		
		while(e.hasMoreElements())
		{
			currentNode = (DefaultMutableTreeNode)e.nextElement();
			System.out.println("removing " + phoneNumber + " from " + currentNode);
			
			if(currentNode.isLeaf())
			{
				list = (ArrayList)currentNode.getUserObject();
				list.remove(list.indexOf(phoneNumber)); 
			}
			else
			{
				map = (HashMap)currentNode.getUserObject();
				map.remove(phoneNumber);
			}
		}
		
		// add pointers between dest and leastCommonAncestor
		e = dest.pathFromAncestorEnumeration(leastCommonAncestor);
		
		while(e.hasMoreElements())
		{
			currentNode = (DefaultMutableTreeNode)e.nextElement();
			System.out.println("adding " + phoneNumber + " to " + currentNode);
			
			if(currentNode.isLeaf())
			{
				list = (ArrayList)currentNode.getUserObject();
				list.add(phoneNumber);
			}
			else
			{
				// find the child of this node that is the
				// ancestor of dest, so we know who to point to
				int i = 0;
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)currentNode.getChildAt(i);
				while(!child.isNodeDescendant(dest) && i < currentNode.getChildCount())
				{
					++i;
					child = (DefaultMutableTreeNode)currentNode.getChildAt(i);
				}
				
				if(i == currentNode.getChildCount())
					return false;
				else
				{
					map = (HashMap)currentNode.getUserObject();
					map.put(phoneNumber, i);
				}
			}
		}
		
		return true;
	}
	
	// this is for testing
	public DefaultMutableTreeNode find(int phoneNumber)
	{
		DefaultMutableTreeNode node = getTop();
		
		// follow pointers down from the root
		while(!node.isLeaf())
		{
			HashMap map = (HashMap)node.getUserObject();
			
			if(map.get(phoneNumber) == null)
				return null;
			else
				node = (DefaultMutableTreeNode)node.getChildAt((Integer)map.get(phoneNumber));
		}
		
		return node;
	}
}
