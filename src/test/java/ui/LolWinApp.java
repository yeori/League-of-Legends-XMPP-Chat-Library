package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import com.github.theholywaffle.lolchatapi.ChatServer;
import com.github.theholywaffle.lolchatapi.FriendRequestPolicy;
import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.listeners.ChatListener;
import com.github.theholywaffle.lolchatapi.listeners.ConnectionListener;
import com.github.theholywaffle.lolchatapi.listeners.FriendListener;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiKey;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;
import com.github.yeori.lol.listeners.MucListener;
import com.github.yeori.lol.muc.Talker;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.JTabbedPane;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

public class LolWinApp {
	private Logger logger = LoggerFactory.getLogger(LolWinApp.class);
	private JFrame frame;
	private JTree friendsTree;
	final private TreeCellRenderer treeCellRenderer = new FriendTreeNodeRenderer();

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
		installUIListeners();
		
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
		friendsTree = new JTree(new FriendTreeModel());
		friendsTree.setCellRenderer(treeCellRenderer);
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
	
	private void installUIListeners () {
		friendsTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getX(), y = e.getY();
				int row = friendsTree.getRowForLocation(x, y);
				TreePath path = friendsTree.getPathForLocation(x, y);
				if ( e.getClickCount() == 2 && row >= 0 ) {
					processDbClickedTreeNode ( (DefaultMutableTreeNode)path.getLastPathComponent());
				}
			}

		});
	}
	
	private void processDbClickedTreeNode(
			DefaultMutableTreeNode dbClickedNode) {
		if ( dbClickedNode.getUserObject() instanceof Friend ) {
			Friend friend = (Friend) dbClickedNode.getUserObject();
			createChatPanel(friend);		
		}
	}

	private void printMessage(Friend friend, String message) {
		
//		Document doc = chatArea.getDocument();
//		try {
//			doc.insertString(doc.getLength(), String.format("[%s] %s", friend.getName(), message), null );
//		} catch (BadLocationException e) {
//			e.printStackTrace();
//		}
		
	}
	
	void createChatPanel(Friend f) {
		ChatPanel cp = new ChatPanel(f);
		tabbedPane.add("[" + f.getName() + "]", cp);
		tabbedPane.setSelectedComponent(cp);
		tabbedPane.setName(f.getUserId());
		logger.debug("creating new tab for chat with {}", f.getUserId());
		
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
				friendsTree.repaint();
			}
			
			@Override
			public void onFriendJoin(Friend friend) {
				// TODO Auto-generated method stub
				System.out.println("들어온 친구 : " + friend);
				friendsTree.repaint();
			}
			
			@Override
			public void onFriendBusy(Friend friend) {
				// TODO Auto-generated method stub
				logger.debug("[바쁨]" + friend);
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
			public void onMucMessage(Talker talker, String body) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean invitationReceived(LolChat chatApi, String roomName,
					String inviter, String password) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		
		LoginDialog loginDialog = new LoginDialog();
		loginDialog.addListener(new LoginDialog.SubmitListener() {
			
			@Override
			public void formSubmitted(Map<String, String> params) {
				processLogin(params.get("id"), params.get("pass"));
			}
		});
		loginDialog.setVisible(true);
	}
	
	private void processLogin(String id, String pass) {
		try {
			this.chatApi.login(id, pass);
			System.out.println("로그인 성공");
			List<Friend> friends = this.chatApi.getFriends();
			showFriends( friends) ;
		} catch( Exception e) {
			e.getCause().printStackTrace();
		}
	}

	private void showFriends(List<Friend> friends) {
		
//		DefaultTreeModel model = new DefaultTree
		final DefaultTreeModel model = (DefaultTreeModel) friendsTree.getModel();
		final MutableTreeNode rootNode = (MutableTreeNode) model.getRoot();
		for ( final Friend f : friends ) {
			f.getName(true); // initialize nickname and jid
			final DefaultMutableTreeNode node = new DefaultMutableTreeNode(f);
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					model.insertNodeInto(node, rootNode, 0);
					System.out.println(f);
					
				}
			});
		}
	}
	
	static class FriendTreeModel extends DefaultTreeModel {
		
		public FriendTreeModel() {
			this( null, true);
		}
		public FriendTreeModel(TreeNode root) {
			this(root, true);
		}

		FriendTreeModel(TreeNode root, boolean asksAllowsChildren) {
			super(root, asksAllowsChildren);
			if ( root == null ) {
				setRoot(new DefaultMutableTreeNode("FRIENDS"));
			}
		}
		
		
	}
	
	static class FriendTreeNodeRenderer implements TreeCellRenderer {

		final private JLabel templateLabel  =new JLabel();
		public FriendTreeNodeRenderer() {
			templateLabel.setOpaque(true);
		}
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			DefaultMutableTreeNode node = DefaultMutableTreeNode.class.cast(value);
			
			if ( node.isRoot()){
				templateLabel.setText(value.toString());
				templateLabel.setForeground(Color.MAGENTA);
			} else {				
				Friend f = Friend.class.cast(node.getUserObject());
				templateLabel.setText(String.format(
						"[%3s] %s", 
						f.isOnline()? "ON": "OFF" ,
								f.getName())
						);
				
				Color fg = f.isOnline() ? Color.BLACK : Color.LIGHT_GRAY ;
				templateLabel.setForeground(fg);
			}
			
			Color bg = selected ? Color.CYAN : Color.WHITE;
			templateLabel.setBackground(bg);
				
			return templateLabel;
		}
	}
}
