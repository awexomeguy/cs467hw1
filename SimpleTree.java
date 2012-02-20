
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
    Object[] hierarchy =
      { "phoneLocation",
        new Object[] { "region1",
              new Object[] { "cell1",
                             "298-112-8786",
                             "298-112-8222",
                             "298-112-2342"},
                            
                        },
        new Object[] { "region2",
                       new Object[] { "cell1",
                                      "573-321-3511",
                                      "573-321-9348",
                                      "573-321-9832"},
                       new Object[] { "cell2",
                                      "573-224-3545",
                                      "573-224-2312"},
                       new Object[] { "cell3",
                                      "573-876-3539",
                                      "573-876-1111",
                                      "573-876-3243",
                                      "573-876-4352"}
                     }
        };
    DefaultMutableTreeNode root = processHierarchy(hierarchy);
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