package ui;

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
