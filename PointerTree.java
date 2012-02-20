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
}