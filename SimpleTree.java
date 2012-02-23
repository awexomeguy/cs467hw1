
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class SimpleTree extends JFrame {
  public static void main(String[] args) {
    new SimpleTree();
  }
 
  public SimpleTree() {
    super("Creating a Simple JTree");
   // WindowUtilities.setNativeLookAndFeel();
    //addWindowListener(new ExitListener());
    Container content = getContentPane();
    
	PointerTree pTree = new PointerTree(3);
    DefaultMutableTreeNode root = pTree.getTop();
    JTree tree = new JTree(root);
    content.add(new JScrollPane(tree), BorderLayout.CENTER);
    setSize(275, 300);
    setVisible(true);
  }

  /** Small routine that will make node out of the first entry
   *  in the array, then make nodes out of subsequent entries
   *  and make them child nodes of the first one. The process is
   *  repeated recursively for entries that are arrays.
   */
   
  private DefaultMutableTreeNode processHierarchy(Object[] hierarchy) {
    DefaultMutableTreeNode node =
      new DefaultMutableTreeNode(hierarchy[0]);
    DefaultMutableTreeNode child;
    for(int i=1; i<hierarchy.length; i++) {
      Object nodeSpecifier = hierarchy[i];
      if (nodeSpecifier instanceof Object[])  // Ie node with children
        child = processHierarchy((Object[])nodeSpecifier);
      else
        child = new DefaultMutableTreeNode(nodeSpecifier); // Ie Leaf
      node.add(child);
    }
    return(node);
  }
}