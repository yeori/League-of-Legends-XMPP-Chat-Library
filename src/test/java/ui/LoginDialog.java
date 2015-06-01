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


import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField idField;
	private JPasswordField passField;
	
	private ArrayList<SubmitListener> listeners = new ArrayList<>();

	public LoginDialog(SubmitListener listener) {
		this();
		addListener(listener);
	}
	/**
	 * Create the dialog.
	 */
	public LoginDialog() {
		setBounds(100, 100, 300, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblLolId = new JLabel("Lol ID");
			GridBagConstraints gbc_lblLolId = new GridBagConstraints();
			gbc_lblLolId.insets = new Insets(0, 0, 5, 5);
			gbc_lblLolId.anchor = GridBagConstraints.EAST;
			gbc_lblLolId.gridx = 0;
			gbc_lblLolId.gridy = 0;
			contentPanel.add(lblLolId, gbc_lblLolId);
		}
		{
			idField = new JTextField();
			GridBagConstraints gbc_idField = new GridBagConstraints();
			gbc_idField.insets = new Insets(0, 0, 5, 0);
			gbc_idField.fill = GridBagConstraints.HORIZONTAL;
			gbc_idField.gridx = 1;
			gbc_idField.gridy = 0;
			contentPanel.add(idField, gbc_idField);
			idField.setColumns(10);
		}
		{
			JLabel lblLolPassword = new JLabel("Lol Password");
			GridBagConstraints gbc_lblLolPassword = new GridBagConstraints();
			gbc_lblLolPassword.anchor = GridBagConstraints.EAST;
			gbc_lblLolPassword.insets = new Insets(0, 0, 5, 5);
			gbc_lblLolPassword.gridx = 0;
			gbc_lblLolPassword.gridy = 1;
			contentPanel.add(lblLolPassword, gbc_lblLolPassword);
		}
		{
			passField = new JPasswordField();
			GridBagConstraints gbc_passField = new GridBagConstraints();
			gbc_passField.insets = new Insets(0, 0, 5, 0);
			gbc_passField.fill = GridBagConstraints.HORIZONTAL;
			gbc_passField.gridx = 1;
			gbc_passField.gridy = 1;
			contentPanel.add(passField, gbc_passField);
		}
		{
			JLabel errorLabel = new JLabel("");
			errorLabel.setForeground(Color.RED);
			GridBagConstraints gbc_errorLabel = new GridBagConstraints();
			gbc_errorLabel.weighty = 1.0;
			gbc_errorLabel.fill = GridBagConstraints.VERTICAL;
			gbc_errorLabel.gridwidth = 2;
			gbc_errorLabel.insets = new Insets(0, 0, 0, 5);
			gbc_errorLabel.gridx = 0;
			gbc_errorLabel.gridy = 2;
			contentPanel.add(errorLabel, gbc_errorLabel);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							notifyToListeners();
						} finally {
							dispose();
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setIDandPass();
	}	
	
	private void setIDandPass() {
		idField.setText("gamja0225");
		passField.setText("fhf1005");
	}
	public void addListener ( SubmitListener listener) {
		if ( listener == null) {
			throw new NullPointerException("submit listener is null ");
		}
		listeners.add(listener);
	}
	
	public void removeListener(SubmitListener listener) {
		listeners.remove(listener);
	}
	
	protected void notifyToListeners () {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HashMap<String, String> params = new HashMap<>();
				params.put("id", idField.getText());
				params.put("pass", new String(passField.getPassword()));
				ArrayList<SubmitListener> cloned = null;
				
				synchronized (listeners) {
					cloned = new ArrayList<>(listeners);
				}
				
				for ( int i = 0 ; i < cloned.size() ; i++) {
					cloned.get(i).formSubmitted(params);
				}
				
			}
		}, "T-LOGIN").start();
	}
	
	public static interface SubmitListener {
		/**
		 * 
		 * @param params - key-value entries
		 */
		public void formSubmitted ( Map<String, String> params ) ;
	}

}
