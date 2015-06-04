package ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;

import ui.renderter.TalkerRenderer;

import com.github.yeori.lol.muc.ChatRoom;
import com.github.yeori.lol.muc.Talker;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GroupChatPanel extends JPanel {
	private ChatRoom room ;
	private JTextField chatInput;
	private TalkerTree<Talker> talkerTrees;
	private JTextPane chatArea;

	
	public GroupChatPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel titlePanel = new JPanel();
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("Group Chat");
		titlePanel.add(lblNewLabel);
		
		JSplitPane splitPane = new JSplitPane();
		add(splitPane, BorderLayout.CENTER);
		
		JScrollPane treeScroll = new JScrollPane();
		splitPane.setLeftComponent(treeScroll);
		
		talkerTrees = new TalkerTree<>();
		talkerTrees.setCellRenderer(new TalkerRenderer());
		treeScroll.setViewportView(talkerTrees);
		
		JPanel chatPanel = new JPanel();
		splitPane.setRightComponent(chatPanel);
		chatPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane chatScroll = new JScrollPane();
		chatPanel.add(chatScroll, BorderLayout.CENTER);
		
		chatArea = new JTextPane();
		chatScroll.setViewportView(chatArea);
		
		chatInput = new JTextField();
		chatInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("INPUT");
			}
		});
		chatPanel.add(chatInput, BorderLayout.SOUTH);
		chatInput.setColumns(10);

	}
	
	public void setChatRoom( ChatRoom room ) {
		this.room = room;
		talkerTrees.update ( room );
	}

	public void addTalker(Talker talker) {
		talkerTrees.addTalkerToTreeView(talker);
	}

	public void addMessage(Talker talker, String body) {
		Document doc = chatArea.getDocument();
		try {
			doc.insertString(doc.getLength(), String.format("[%s] %s\n", talker.getName(), body), null);
			
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void removeTalker(Talker talker) {
		DefaultMutableTreeNode node = talkerTrees.findFriendNodeByJID(talker.getUserId());
		talkerTrees.removeTreeNode(node);
	}

}
