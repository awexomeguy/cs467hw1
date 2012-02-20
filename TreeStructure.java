import java.util.*;
import javax.swing.tree.*;

public class TreeStructure
{
	private int targetHeight;
	private int targetWidth;
	private DefaultMutableTreeNode top;
	
	public TreeStructure(int h, int w)
	{
		targetHeight = h;
		targetWidth = w;

		// create the root node
		top = new DefaultMutableTreeNode();

		System.out.println("created the tree");
		
		// recursively build the tree from the root
		createChildrenFor(top);
	}
	
	// recursively populates the subtree starting at this node,
	// with each node having targetWidth number of children, until
	// targetHieght number of levels have been built
	public void createChildrenFor(DefaultMutableTreeNode node)
	{
		for(int i = 0; i < targetWidth; ++i)
		{
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();
			node.add(newNode);
		}
		
		if(node.getLevel() < (targetHeight - 1))
			for(int n = 0; n < node.getChildCount(); ++n)
				createChildrenFor((DefaultMutableTreeNode)node.getChildAt(n));
	}
	
	// prints out the hierarchy of the tree on the screen
	public void printTreeStartingAt(DefaultMutableTreeNode node)
	{		
		Enumeration e = node.breadthFirstEnumeration();
		for(; e.hasMoreElements();)
			System.out.println(e.nextElement());
	}
	
	public DefaultMutableTreeNode getTop()
	{
		return top;
	}
}
