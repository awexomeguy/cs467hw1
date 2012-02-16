import java.util.*;
import javax.swing.tree.*;

public class TreeApp
{
	static int j = 1;
	static int targetHeight;
	static int targetWidth;
	
	public static void main(String [] args)
	{
		try
		{
			targetHeight = Integer.parseInt(args[0]);
		}catch(NumberFormatException e){
			System.err.println("Argument must be an integer");
			System.exit(1);
		}
		
		try
		{
			targetWidth = Integer.parseInt(args[1]);
		}catch(NumberFormatException e){
			System.err.println("Argument must be an integer");
			System.exit(1);
		}

		// create the root node
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(0);

		System.out.println("created the tree");
		
		// recursively build the tree from the root
		createChildrenFor(top);
		
		printTreeStartingAt(top);
			
		System.out.println("");
	}
	
	// recursively populates the subtree starting at this node,
	// with each node having targetWidth number of children, until
	// targetHieght number of levels have been built
	public static void createChildrenFor(DefaultMutableTreeNode node)
	{
		for(int i = 0; i < targetWidth; ++i, ++j)
		{
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(j);
			node.add(newNode);
		}
		
		if(node.getLevel() < (targetHeight - 1))
			for(int n = 0; n < node.getChildCount(); ++n)
				createChildrenFor((DefaultMutableTreeNode)node.getChildAt(n));
	}
	
	// prints out the hierarchy of the tree on the screen
	public static void printTreeStartingAt(DefaultMutableTreeNode node)
	{		
		Enumeration e = node.breadthFirstEnumeration();
		for(; e.hasMoreElements();)
			System.out.println(e.nextElement());
	}
}