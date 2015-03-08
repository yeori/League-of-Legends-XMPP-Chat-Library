package ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTextField;

import com.github.theholywaffle.lolchatapi.ChatServer;
import com.github.theholywaffle.lolchatapi.FriendRequestPolicy;
import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.listeners.ChatListener;
import com.github.theholywaffle.lolchatapi.listeners.ConnectionListener;
import com.github.theholywaffle.lolchatapi.listeners.FriendListener;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiKey;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;
import com.github.yeori.lol.listeners.MucListener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JTabbedPane;

public class LolWinApp {

	private JFrame frame;
	private JTree friendsTree;

	private LolChat chatApi ;
	/**
	 * Launch the application.
	 */
	static LolWinApp window ;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JTabbedPane tabbedPane;
	public static void main(String[] args) {
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new LolWinApp();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	/**
	 * Create the application.
	 */
	public LolWinApp() {
		String key = "0f9fc925-34db-4b5d-8bb5-98fc437a815b";
		chatApi = new LolChat(ChatServer.KR, 
				FriendRequestPolicy.MANUAL, 
				new RiotApiKey(key));
		
		initialize();
		
		
	}
	
	public void login ( String id, String pass) {
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JSplitPane splitPane = new JSplitPane();
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		friendsTree = new JTree();
		scrollPane.setViewportView(friendsTree);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		splitPane.setRightComponent(tabbedPane);
		
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmLogin = new JMenuItem("Login");
		mntmLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showLoginDialog();
			}
		});
		mnFile.add(mntmLogin);
	}

	private void printMessage(Friend friend, String message) {
		
//		Document doc = chatArea.getDocument();
//		try {
//			doc.insertString(doc.getLength(), String.format("[%s] %s", friend.getName(), message), null );
//		} catch (BadLocationException e) {
//			e.printStackTrace();
//		}
		
	}
	
	void createChat(Friend f) {
		ChatPanel cp = new ChatPanel(f);
		tabbedPane.add("[" + f.getName(true) + "]", cp);
		tabbedPane.setSelectedComponent(cp);
	}
	private void showLoginDialog() {
		JFrame parent = this.frame;
		
		this.chatApi.addChatListener(new ChatListener() {
			
			@Override
			public void onMessage(Friend friend, String message) {
				printMessage ( friend , message);
				
			}

		});
		
		this.chatApi.addFriendListener(new FriendListener() {
			
			@Override
			public void onRemoveFriend(String userId, String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onNewFriend(Friend friend) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFriendStatusChange(Friend friend) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFriendLeave(Friend friend) {
				// TODO Auto-generated method stub
			System.out.println("나간 친구 : " + friend);
				
			}
			
			@Override
			public void onFriendJoin(Friend friend) {
				// TODO Auto-generated method stub
				System.out.println("들어온 친구 : " + friend);
				
			}
			
			@Override
			public void onFriendBusy(Friend friend) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFriendAway(Friend friend) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFriendAvailable(Friend friend) {
				// TODO Auto-generated method stub
				
			}
		});
		
		this.chatApi.addConnectionListener(new ConnectionListener() {
			
			@Override
			public void reconnectionSuccessful() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void reconnectionFailed(Exception e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void reconnectingIn(int seconds) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void connectionClosedOnError(Exception e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void connectionClosed() {
				// TODO Auto-generated method stub
				
			}
		});
		
		this.chatApi.addMultiUserChatListener(new MucListener() {
			
			@Override
			public void onMucMessage(Friend sender, String body) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean invitationReceived(LolChat chatApi, String roomName,
					String inviter, String password) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		
		
		if ( this.chatApi.login("gamja0225", "fhf1005") ) {
			System.out.println("로그인 성공");
			List<Friend> friends = this.chatApi.getFriends();
			showFriends( friends) ;
			createChat(chatApi.getFriendByName("양파님이시다"));
		}
		
	}

	private void showFriends(List<Friend> friends) {
		
		DefaultMutableTreeNode node ;
//		DefaultTreeModel model = new DefaultTree
		
		for ( Friend f : friends ) {
			node = new DefaultMutableTreeNode(f);
			System.out.println(f);
		}
	}

}
