package ui.renderter;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import com.github.yeori.lol.muc.Talker;

public class TalkerRenderer implements TreeCellRenderer {

	final private JLabel templateLabel  =new JLabel();
	public TalkerRenderer() {
		templateLabel.setOpaque(true);
	}
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		DefaultMutableTreeNode node = DefaultMutableTreeNode.class.cast(value);
		
		StringBuilder text = new StringBuilder();
		Color fg = Color.RED;
		Color bg = Color.WHITE;
		if ( node.isRoot()){
			fg = Color.BLACK;
			bg = Color.MAGENTA;
			text.append( value.toString());
		} else {
			Talker talker = Talker.class.cast(node.getUserObject());

			fg = Color.GREEN.darker();
			bg = selected ? Color.CYAN : Color.WHITE;
			
			text.append (String.format("[%s]",	talker.getName()) );
		}
		
		templateLabel.setText(text.toString());
		templateLabel.setForeground(fg);
		templateLabel.setBackground(bg);
			
		return templateLabel;
	}
}