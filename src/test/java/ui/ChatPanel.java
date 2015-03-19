package ui;

import javax.swing.JPanel;

import com.github.theholywaffle.lolchatapi.wrapper.Friend;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ChatPanel extends JPanel {

	private Logger logger = LoggerFactory.getLogger(ChatPanel.class);
	
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

//		chatter.setChatListener(this);
	}
	
	public void sendMessage ( ) {
		String msg = textField.getText();
		chatter.sendMessage(msg);
		printMessage("[나]" + msg );
	}

	public void printMessage(Friend friend, String message) {
		printMessage (String.format("[%s] %s", friend.getName(), message));
	}
	
	private void printMessage ( String msg) {
		StyledDocument doc = messagePane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), msg + "\n", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	

}
