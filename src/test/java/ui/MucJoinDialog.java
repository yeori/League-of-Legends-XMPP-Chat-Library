package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Font;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MucJoinDialog extends JDialog {
	private Logger logger = LoggerFactory.getLogger(MucJoinDialog.class);
	private final JPanel contentPanel = new JPanel();
	private JTextField roomNameField;

	private List<RoomNameListener> listeners = new ArrayList<>();
//	/**
//	 * Launch the application.
//	 */
//	public static void main(String[] args) {
//		try {
//			MucJoinDialog dialog = new MucJoinDialog();
//			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//			dialog.setVisible(true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Create the dialog.
	 */
	public MucJoinDialog() {
		setBounds(100, 100, 300, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblRoom = new JLabel("Room");
			lblRoom.setFont(new Font("Tahoma", Font.BOLD, 18));
			GridBagConstraints gbc_lblRoom = new GridBagConstraints();
			gbc_lblRoom.insets = new Insets(0, 0, 0, 5);
			gbc_lblRoom.anchor = GridBagConstraints.EAST;
			gbc_lblRoom.gridx = 0;
			gbc_lblRoom.gridy = 0;
			contentPanel.add(lblRoom, gbc_lblRoom);
		}
		{
			roomNameField = new JTextField();
			roomNameField.setFont(new Font("Gulim", Font.BOLD, 18));
			GridBagConstraints gbc_roomNameField = new GridBagConstraints();
			gbc_roomNameField.fill = GridBagConstraints.HORIZONTAL;
			gbc_roomNameField.gridx = 1;
			gbc_roomNameField.gridy = 0;
			contentPanel.add(roomNameField, gbc_roomNameField);
			roomNameField.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						notifyNewRoomName(roomNameField);
						dispose();
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
	}
	
	private void notifyNewRoomName(JTextField nameField) {
		String name = nameField.getText().trim();
		if ( "".equals(name)) {
			logger.debug("invalid room name : [" + name + "]");
			return ;
		}
		Iterator<RoomNameListener> itr = listeners.iterator();
		while ( itr.hasNext()) {
			itr.next().roomNameInput(name);
		}
	}
	
	public void addRoomNameListener ( RoomNameListener rl) {
		if ( this.listeners.contains(rl) ) return ;
		
		this.listeners.add(rl);
	}
	
	public static interface RoomNameListener {
		public void roomNameInput(String roomName) ;
	}

}
