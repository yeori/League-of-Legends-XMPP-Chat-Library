package ui.renderter;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import com.github.theholywaffle.lolchatapi.wrapper.Friend;
import com.github.theholywaffle.lolchatapi.wrapper.Friend.FriendStatus;

public class FriendTreeNodeRenderer implements TreeCellRenderer {

	final private JLabel templateLabel  =new JLabel();
	public FriendTreeNodeRenderer() {
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
			Friend f = Friend.class.cast(node.getUserObject());

			fg = f.isOnline() ? Color.GREEN.darker() : Color.GRAY;
			bg = selected ? Color.CYAN : Color.WHITE;
			
			text.append (String.format("[%3s] %s",
					f.isOnline()? "ON": "OFF" ,
					f.getName()) );
			if ( f.getFriendStatus() == FriendStatus.MUTUAL_FRIENDS ) {
				text.append("<>");
			}
		}
		
		templateLabel.setText(text.toString());
		
//		olor bg = selected ? Color.CYAN : Color.WHITE;
		templateLabel.setForeground(fg);
		templateLabel.setBackground(bg);
			
		return templateLabel;
	}
}