package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.Font;

public class FriendRegisterDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField summonerNameField;
	
	NewFriendRequest listener ;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			FriendRegisterDialog dialog = new FriendRegisterDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public FriendRegisterDialog(JFrame parent) {
		setBounds(100, 100, 300, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblSummonerName = new JLabel("Summoner Name");
			lblSummonerName.setFont(new Font("Tahoma", Font.BOLD, 18));
			GridBagConstraints gbc_lblSummonerName = new GridBagConstraints();
			gbc_lblSummonerName.insets = new Insets(0, 0, 5, 0);
			gbc_lblSummonerName.gridx = 0;
			gbc_lblSummonerName.gridy = 0;
			contentPanel.add(lblSummonerName, gbc_lblSummonerName);
		}
		{
			summonerNameField = new JTextField();
			summonerNameField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					processRegisteringNewFriend();
				}
			});
			summonerNameField.setFont(new Font("굴림", Font.PLAIN, 18));
			GridBagConstraints gbc_summonerNameField = new GridBagConstraints();
			gbc_summonerNameField.fill = GridBagConstraints.HORIZONTAL;
			gbc_summonerNameField.gridx = 0;
			gbc_summonerNameField.gridy = 1;
			contentPanel.add(summonerNameField, gbc_summonerNameField);
			summonerNameField.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						processRegisteringNewFriend();
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
						closeDialog();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public static interface NewFriendRequest {
		public void newFriendRegisteringRequest(String friendName) ;
	}
	
	private void processRegisteringNewFriend() {
		String summonerName = summonerNameField.getText().trim();
		if ( listener != null ) {
			listener.newFriendRegisteringRequest(summonerName);
		}
		closeDialog();
	}
	
	public void closeDialog() {
		this.dispose();
	}

}
