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


import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.github.theholywaffle.lolchatapi.ChatMode;
import com.github.theholywaffle.lolchatapi.ChatServer;
import com.github.theholywaffle.lolchatapi.FriendRequestPolicy;
import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.listeners.ChatListener;
import com.github.theholywaffle.lolchatapi.listeners.ConnectionListener;
import com.github.theholywaffle.lolchatapi.listeners.FriendListener;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiKey;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;
import com.github.yeori.lol.listeners.MucListener;
import com.github.yeori.lol.muc.ChatRoom;
import com.github.yeori.lol.muc.Talker;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.JTabbedPane;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import ui.FriendRegisterDialog.NewFriendRequest;
import ui.renderter.FriendTreeNodeRenderer;

public class LolWinApp {
	private Logger logger = LoggerFactory.getLogger(LolWinApp.class);
	private JFrame frame;
//	private JTree friendsTree;
	private TalkerTree<Friend> friendsTree;
	
	private LolChat chatApi ;
	private GroupChatManager gcManager = new GroupChatManager();
	
	/**
	 * Launch the application.
	 */
	static LolWinApp window ;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JTabbedPane tabbedPane;
	private JMenu mnMuc;
	private JMenuItem mntmJoin;
	private JMenu mnFriends;
	private JMenuItem mntmAddNewFriend;
	private JMenuItem mntmRemovefriend;
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
		friendsTree = new TalkerTree<Friend>();
		friendsTree.setCellRenderer(new FriendTreeNodeRenderer());
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
		
		mnMuc = new JMenu("MUC");
		menuBar.add(mnMuc);
		
		mntmJoin = new JMenuItem("join...");
		mntmJoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showMucJoinDialog();
			}
		});
		mnMuc.add(mntmJoin);
		
		mnFriends = new JMenu("Friends");
		menuBar.add(mnFriends);
		
		mntmAddNewFriend = new JMenuItem("Add New Friend");
		mntmAddNewFriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showFriendRegisteringDialog();
			}
		});
		mnFriends.add(mntmAddNewFriend);
		
		mntmRemovefriend = new JMenuItem("RemoveFriend");
		mntmRemovefriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				processFriendRemoval();
			}
		});
		mnFriends.add(mntmRemovefriend);
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
	
	private void processFriendRemoval() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) friendsTree
				.getSelectionPath()
				.getLastPathComponent();
		Object userObj = node.getUserObject();
		if ( userObj.getClass() != Friend.class) {
			return ;
		}
		
		Friend friend = (Friend) userObj ;
		chatApi.removeFriend(friend);
	}
	
	private void processDbClickedTreeNode(
			DefaultMutableTreeNode dbClickedNode) {
		if ( dbClickedNode.getUserObject() instanceof Friend ) {
			Friend friend = (Friend) dbClickedNode.getUserObject();
			createChatPanel(friend);
		}
	}

	private void printMessage(Friend friend, String message) {
		ChatPanel chatPanel = findOpenTab(tabbedPane, friend.getName());
		
		if( chatPanel == null ) {
			chatPanel = createChatPanel(friend);
		}
		tabbedPane.setSelectedComponent(chatPanel);
		chatPanel.printMessage ( friend, message);
	}
	
	private ChatPanel findOpenTab(JTabbedPane tabPane, String friendName) {
		int size = tabPane.getTabCount();
		ChatPanel chatPanel = null;
		for( int i =  0 ; i < size; i++) {
			chatPanel = (ChatPanel) tabPane.getComponentAt(i) ;
			if ( chatPanel.getName().equals(friendName)) {
				return chatPanel;
			}
		}
		return null;
	}
	
	ChatPanel createChatPanel(Friend f) {
		ChatPanel cp = new ChatPanel(f);
		cp.setName(f.getName());
		tabbedPane.add("[" + f.getName() + "]", cp);
		tabbedPane.setSelectedComponent(cp);
		tabbedPane.setName(f.getUserId());
		logger.debug("creating new tab for chat with {}", f.getUserId());
		return cp;
		
	}
	
	GroupChatPanel createGroupChatPanel( ChatRoom room ) {
		GroupChatPanel gcp = new GroupChatPanel();
		gcp.setChatRoom(room);
		gcp.setName(room.getRoomName());
		
		tabbedPane.add("[GROUP CHAT]", gcp);
		tabbedPane.setSelectedComponent(gcp);
		tabbedPane.setName(room.getRoomName());
		logger.debug("creating new tab for GROUP CHAT", room.getRoomName());
		
		gcManager.addPanel(room, gcp);
		return gcp;
	}
	
	private void showMucJoinDialog() {
		MucJoinDialog dialog = new MucJoinDialog();
		dialog.addRoomNameListener(new MucJoinDialog.RoomNameListener() {
			
			@Override
			public void roomNameInput(String roomName) {
				ChatRoom room = chatApi.joinPublicRoom(roomName, new MucListener() {
					
					@Override
					public void onMucMessage(Talker talker, String body) {
						logger.debug("[MUC MESSAGE: {} ] {} ", talker.getNickName(), body );
					}
					
					@Override
					public void invitationReceived(LolChat chatApi, String roomName,
							String inviter, String password) {
						// TODO Auto-generated method stub
						logger.debug(String.format("방 초대 요청 : %s by %s", roomName, inviter));
					}
					
					@Override
					public void newTalkerEntered(ChatRoom chatRoom, Talker newTalker) {
						GroupChatPanel gcPanel = (GroupChatPanel) gcManager.getTabPanel(chatRoom);
						gcPanel.addTalker ( newTalker );
						
					}
					
					@Override
					public void chatModeChanged(ChatRoom chatRoom, Talker talker,
							ChatMode chatMode) {
						logger.debug("[채팅 모드 변경] " + chatMode + " of " + talker.getName());
					}
					
					@Override
					public void talkerLeaved(ChatRoom chatRoom, Talker talker) {
						logger.debug("나간 사용자 : " + talker);
					}
				});
			}
		});
		dialog.setVisible(true);
	}
	
	/**
	 * 새로운 친구 추가 대화창
	 */
	private void showFriendRegisteringDialog() {
		
		FriendRegisterDialog dialog = new FriendRegisterDialog(frame);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		dialog.listener = new NewFriendRequest() {
			
			@Override
			public void newFriendRegisteringRequest(String friendName) {
				chatApi.addFriendByName(friendName);
			}
		};
		
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
				logger.debug("제거된 친구 :  " + name + ", id: " + userId);
				DefaultMutableTreeNode node = friendsTree.findFriendNodeByJID ( userId ) ;
				friendsTree.removeTreeNode ( node );
				
			}
			
			@Override
			public void onNewFriend(Friend friend) {
				logger.debug("새로운 친구 :  " + friend.toString());
				friendsTree.addTalkerNode(friend);
			}
			
			@Override
			public void onFriendStatusChange(Friend friend) {
				// TODO Auto-generated method stub
				logger.debug("친구 상태 변경 : " + friend);
			}
			
			@Override
			public void onFriendLeave(Friend friend) {
				// TODO Auto-generated method stub
				logger.debug("나간 친구 : " + friend);
				friendsTree.repaint();
			}
			
			@Override
			public void onFriendJoin(Friend friend) {
				logger.debug("들어온 친구 : " + friend);
				friendsTree.repaint();
			}
			
			@Override
			public void onFriendBusy(Friend friend) {
				logger.debug("[바쁨]" + friend);
			}
			
			@Override
			public void onFriendAway(Friend friend) {
				logger.debug("[AWAY]" + friend);
			}
			
			@Override
			public void onFriendAvailable(Friend friend) {
				logger.debug("[AVAILABLE]" + friend);
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
				ChatRoom room = talker.getRoom();
				GroupChatPanel gcp = gcManager.getTabPanel(room);
				logger.debug(String.format("[%s at %s] %s", talker.getName(), talker.getRoom().getRoomName(), body));
				gcp.addMessage ( talker, body );
			}
			
			@Override
			public void invitationReceived(LolChat chatApi, String roomName,
					String inviter, String password) {
				processInvitation( roomName, inviter);
			}
			
			@Override
			public void newTalkerEntered(ChatRoom chatRoom, Talker newTalker) {
				logger.debug( String.format("[%s] " + newTalker, chatRoom.getRoomName() ));
				GroupChatPanel gcPanel = (GroupChatPanel) gcManager.getTabPanel(chatRoom);
				gcPanel.addTalker ( newTalker );
			}
			
			@Override
			public void chatModeChanged(ChatRoom chatRoom, Talker talker,
					ChatMode chatMode) {
				logger.debug("[채팅 모드 변경] " + chatMode + " of " + talker.getName());
			}
			
			@Override
			public void talkerLeaved(ChatRoom chatRoom, Talker talker) {
				logger.debug("나간 사용자 : " + talker);
				GroupChatPanel gcPanel = (GroupChatPanel) gcManager.getTabPanel(chatRoom);
				gcPanel.removeTalker ( talker );
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
	
	private void processInvitation(String roomName, String inviter) {
		String message = String.format("%s님이 비공개채팅방으로 초대함. 참여할래?", inviter);
		int result = JOptionPane.showConfirmDialog(this.frame, message, "초대", JOptionPane.YES_NO_OPTION);
		if ( result == JOptionPane.NO_OPTION) {
			return ;
		} 
		
		System.out.println("참여");
		GroupChatPanel panel = gcManager.findByIdRoomName ( roomName );
		if ( panel != null ) {
			// existing room
			logger.debug( "existing room: " + roomName);
			tabbedPane.setSelectedComponent(panel);
			return ;
		}
		ChatRoom room = chatApi.joinPrivateRoom(roomName);
		GroupChatPanel groupPanel = createGroupChatPanel(room);
		groupPanel.setChatRoom(room);
	}

	private void processLogin(String id, String pass) {
		try {
			this.chatApi.login(id, pass);
			System.out.println("로그인 성공");
			List<Friend> friends = this.chatApi.getFriends();
			friendsTree.showTalkers( friends) ;
		} catch( Exception e) {
			e.getCause().printStackTrace();
		}
	}
	
	static class GroupChatManager {
		Map<String, GroupChatPanel> gcMap;
		
		public GroupChatManager() {
			gcMap = new HashMap<>();
		}
		
		public GroupChatPanel findByIdRoomName(String roomName) {
			return gcMap.get(roomName);
		}

		void addPanel (ChatRoom room, GroupChatPanel comp) {
			gcMap.put(room.getRoomName(), comp);
		}
		
		GroupChatPanel getTabPanel ( ChatRoom room ) {
			return gcMap.get(room.getRoomName());
		}
		
	}
}
