package ui.renderter;

/*
 * #%L
 * League of Legends XMPP Chat Library
 * %%
 * Copyright (C) 2014 - 2015 Bert De Geyter
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


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