package ui;

import javax.swing.JPanel;

import com.github.theholywaffle.lolchatapi.listeners.ChatListener;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ChatPanel extends JPanel implements ChatListener {

	private Friend chatter ;
	private JTextField textField;
	private JTextPane messagePane;
	/**
	 * Create the panel.
	 */
	public ChatPanel(Friend chatter ) {
		setBorder(new EmptyBorder(4, 4, 4, 4));
		this.chatter = chatter;
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		messagePane = new JTextPane();
		scrollPane.setViewportView(messagePane);
		
		textField = new JTextField();
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMessage();
			}
		});
		add(textField, BorderLayout.SOUTH);
		textField.setColumns(10);

		chatter.setChatListener(this);
	}
	
	public void sendMessage ( ) {
		String msg = textField.getText();
		chatter.sendMessage(msg);
	}

	@Override
	public void onMessage(Friend friend, String message) {
		StyledDocument doc = messagePane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), String.format("[%s] %s\n", friend.getName(), message), null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	

}
