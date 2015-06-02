package com.github.theholywaffle.lolchatapi;

/*
 * #%L
 * League of Legends XMPP Chat Library
 * %%
 * Copyright (C) 2014 Bert De Geyter
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Bind;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Session;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.PacketParserUtils.UnparsedResultIQ;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.lolchatapi.listeners.ChatListener;
import com.github.theholywaffle.lolchatapi.listeners.ConnectionListener;
import com.github.theholywaffle.lolchatapi.listeners.FriendListener;
import com.github.theholywaffle.lolchatapi.listeners.FriendRequestListener;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApi;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiException;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiKey;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;
import com.github.theholywaffle.lolchatapi.wrapper.Friend.FriendStatus;
import com.github.theholywaffle.lolchatapi.wrapper.FriendGroup;
import com.github.yeori.lol.Login;
import com.github.yeori.lol.listeners.MucListener;
import com.github.yeori.lol.muc.ChatRoom;
import com.github.yeori.lol.muc.IRoomNaming;
import com.github.yeori.lol.muc.MucException;
import com.github.yeori.lol.muc.RoomNamings;
import com.github.yeori.lol.riotapi.DefaultRiotApiFactory;
import com.github.yeori.lol.riotapi.RiotApiFactory;

public class LolChat {
	private Logger logger = LoggerFactory.getLogger(LolChat.class);
	private final XMPPConnection connection;
	private final List<ChatListener> one2oneChatListeners = new ArrayList<>();
	private final List<MucListener> mucChatListeners = new ArrayList<>();
	
	private final List<FriendListener> friendListeners = new ArrayList<>();
	private final List<ConnectionListener> connectionListeners = new ArrayList<>();
	
//	private boolean stops = false;

	private LolStatus status = new LolStatus();
	private final Presence.Type type = Presence.Type.available;
	private Presence.Mode mode = Presence.Mode.chat;
	private boolean invisible = false;
	
	
	private final List<Friend> friends = new ArrayList<>();
	
	/**
	 * full qualified jabberID
	 * ex) sum33072235@pvp.net/xiff
	 */
	private String jabberID;
	
	/**
	 * summer name(unique nickname in the game)
	 */
	private String name = null;
	
	private IRoomNaming roomNaming;
	private LeagueRosterListener leagueRosterListener;
	private LeaguePacketListener leaguePacketListener;
	private MucHandle mucHandler;
	private FriendRequestPolicy friendRequestPolicy;
	private boolean loaded;
	private RiotApi riotApi;
	private final ChatServer server;
	private ConnectionConfiguration xmppConfig;

	/**
	 * Represents a single connection to a League of Legends chatserver. Default
	 * FriendRequestPolicy is {@link FriendRequestPolicy#ACCEPT_ALL}.
	 * 
	 * @see FriendRequestPolicy
	 * 
	 * @param server
	 *            The chatserver of the region you want to connect to
	 */
	public LolChat(ChatServer server) {
		this(server, FriendRequestPolicy.ACCEPT_ALL, null);
	}

	/**
	 * Represents a single connection to a League of Legends chatserver.
	 * 
	 * @param server
	 *            The chatserver of the region you want to connect to
	 * @param friendRequestPolicy
	 *            Determines how new Friend requests are treated.
	 * 
	 * @see LolChat#setFriendRequestPolicy(FriendRequestPolicy)
	 * @see LolChat#setFriendRequestListener(FriendRequestListener)
	 */
	public LolChat(ChatServer server, FriendRequestPolicy friendRequestPolicy) {
		this(server, friendRequestPolicy, null);
	}

	/**
	 * Represents a single connection to a League of Legends chatserver.
	 * 
	 * @param server
	 *            The chatserver of the region you want to connect to
	 * @param friendRequestPolicy
	 *            Determines how new Friend requests are treated.
	 * @param riotApiKey
	 *            Your apiKey used to convert summonerId's to name. You can get
	 *            your key here <a
	 *            href="https://developer.riotgames.com/">developer
	 *            .riotgames.com</a>
	 * 
	 * @see LolChat#setFriendRequestPolicy(FriendRequestPolicy)
	 * @see LolChat#setFriendRequestListener(FriendRequestListener)
	 */
	public LolChat(ChatServer server, FriendRequestPolicy friendRequestPolicy,
			RiotApiKey riotApiKey) {
		this(server, 
				friendRequestPolicy, 
				riotApiKey, 
				SSLSocketFactory.getDefault(),
				new DefaultRiotApiFactory() ,
				RoomNamings.createPublicRoomNaming());
	}
	
	public LolChat(ChatServer server, FriendRequestPolicy friendRequestPolicy,
			RiotApiKey riotApiKey, SocketFactory default1,
			DefaultRiotApiFactory defaultRiotApiFactory) {
		this(server, 
				friendRequestPolicy, 
				riotApiKey, 
				SSLSocketFactory.getDefault(),
				new DefaultRiotApiFactory() ,
				RoomNamings.createPublicRoomNaming());
	}

	/**
	 * 
	 * @param server
	 * @param friendRequestPolicy
	 * @param riotApiKey
	 * @param sFactory
	 * @param riotApiFactory
	 * @param roomNaming
	 */
	public LolChat(ChatServer server, 
			FriendRequestPolicy friendRequestPolicy, 
			RiotApiKey riotApiKey, 
			SocketFactory sFactory,
			RiotApiFactory riotApiFactory,
			IRoomNaming roomNaming) {
		logger.debug(String.format("server : %s:%s", server.host, server.port));
		logger.debug(String.format("friend-request-mode : %s", friendRequestPolicy));
		logger.debug(String.format("riot-key : %s", riotApiKey.getKey()));
		
		this.friendRequestPolicy = FriendRequestPolicy.MANUAL;
		this.server = server;
		this.riotApi = riotApiFactory.createRiotApi(riotApiKey, server);
		
		Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);
		xmppConfig = new ConnectionConfiguration(
				server.host, 
				server.port, 
				"pvp.net");
		
		xmppConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
		xmppConfig.setSocketFactory(SSLSocketFactory.getDefault());
		xmppConfig.setCompressionEnabled(true);
		connection = new XMPPTCPConnection(xmppConfig);
		
		this.roomNaming = roomNaming;
		addListeners();
	}

	private void addListeners() {
		installInitConnListener();
		installDefaultPacketListener();
		installDefaultConnectionListener();
		installDefaultRosterListener();
		
		installOnetoOneChatListener();
		installMucChatListener();
	}
	
	private void installInitConnListener() {
		InitialConnListener icl = new InitialConnListener();
		IncomingPackets incoming = new IncomingPackets();
		PacketFilter every = new PacketFilter() {
			
			@Override
			public boolean accept(Packet packet) {
//				logger.debug("cls:" + packet.getClass());
				return true;
			}
		};
		connection.addPacketSendingListener(icl, every);
		connection.addPacketListener(incoming, every);
		
	}
	
	class IncomingPackets implements PacketListener {

		@Override
		public void processPacket(Packet packet) throws NotConnectedException {
			Class<?> packetClass = packet.getClass();
			String cname = packet.getClass().getName();
			cname = cname.substring(cname.lastIndexOf('.')+1);
			logger.debug( String.format("<<[%s] %s",cname, packet.toXML()) );
			if ( packetClass == Bind.class) {
				Bind bnd = (Bind) packet;
				if ( bnd.getType() == IQ.Type.RESULT){
					logger.debug("    JID : " + bnd.getJid());
					jabberID = bnd.getJid();
				}
			} else if ( packetClass == PacketParserUtils.UnparsedResultIQ.class) {
				UnparsedResultIQ riq = (UnparsedResultIQ) packet;
				logger.debug("   " +  riq.getChildElementXML() );
			}
		}
		
	}
	
	class InitialConnListener implements PacketListener {
		
		@Override
		public void processPacket(Packet packet) throws NotConnectedException {
			Class<?> packetClass = packet.getClass();
			
			if ( packetClass == Bind.class) {
				Bind bnd = Bind.class.cast(packet);
				logger.debug(String.format(">>[BIND] bind type: %s, xml: %s", bnd.getType(), bnd.toXML().toString()));
				
				
			} else if ( packetClass == Session.class) {
				Session iq = Session.class.cast(packet);
				logger.debug(">>[SESSION] " + iq.toXML().toString());
				if ( iq.getType() == IQ.Type.RESULT){
					logger.debug(">>  SESSION : " + iq.getChildElementXML());
				}
			} else if ( packetClass == UnparsedResultIQ.class) {
				UnparsedResultIQ uiq = UnparsedResultIQ.class.cast(packet);
				if ( uiq.getType() == IQ.Type.RESULT){
					String xml = uiq.getChildElementXML();
					String name = xml.substring(0, xml.indexOf('<'));
					logger.debug(">>  SUMMONER NAME : " + name);
					LolChat.this.name = name;
//					connection.removePacketListener(this);
//					connection.removePacketSendingListener(this);
				}
			}
			
			else {
				logger.debug( String.format(">>[PACKET] %s", packet.toXML().toString()) );				
			}
			
		}
		
	}
	private void installDefaultPacketListener() {
		leaguePacketListener = new LeaguePacketListener(this, connection);
		connection.addPacketListener(
				leaguePacketListener, new PacketFilter() {
					public boolean accept(Packet packet) {
						if (packet instanceof Presence) {
							final Presence presence = (Presence) packet;
							if (presence.getType().equals(
									Presence.Type.subscribed)
									|| presence.getType().equals(
											Presence.Type.subscribe)
									|| presence.getType().equals(
											Presence.Type.unsubscribed)
									|| presence.getType().equals(
											Presence.Type.unsubscribe)) {
								return true;
							}
						}
						return false;
					}
				});
	}
	
	private void installDefaultConnectionListener() {
		connection
		.addConnectionListener(new org.jivesoftware.smack.ConnectionListener() {
			
			public void authenticated(XMPPConnection connection) {
				logger.debug(String.format("IS AUTHENTICATED : %s", connection.isAuthenticated()));
			}
			
			public void connected(XMPPConnection connection) {
				logger.debug(String.format("IS CONNECTED : %s", connection.isConnected()));
			}
			
			public void connectionClosed() {
				logger.debug("CONNECTION CLOSED, notifying to listeners");
				for (final ConnectionListener l : connectionListeners) {
					l.connectionClosed();
				}
			}
			
			public void connectionClosedOnError(Exception e) {
				logger.debug("CONNECTION CLOSED because of error", e);
				for (final ConnectionListener l : connectionListeners) {
					l.connectionClosedOnError(e);
				}
			}
			
			public void reconnectingIn(int seconds) {
				logger.debug("RECONNECTING IN {} secs", seconds);
				for (final ConnectionListener l : connectionListeners) {
					l.reconnectingIn(seconds);
				}
			}
			
			public void reconnectionFailed(Exception e) {
				logger.debug("RECONNECTION FAILED. cause", e);
				for (final ConnectionListener l : connectionListeners) {
					l.reconnectionFailed(e);
				}
			}
			
			public void reconnectionSuccessful() {
				logger.debug("RECONNECTION SUCCESS");
				updateStatus();
				for (final ConnectionListener l : connectionListeners) {
					l.reconnectionSuccessful();
				}
			}
		});
	}
	private void installDefaultRosterListener() {
		leagueRosterListener = new LeagueRosterListener(this,connection);
		connection.getRoster().addRosterListener(leagueRosterListener);
	}
	

	
	private void installOnetoOneChatListener() {
		ChatManagerListener one2oneListener = new ChatManagerListener() {

			@Override
			public void chatCreated(final Chat c, final boolean locally) {

				final Friend friend = getFriendById(c.getParticipant());
				if (friend != null) {
					c.addMessageListener(new MessageListener() {

						@Override
						public void processMessage(Chat chat,
								Message msg) {
							if (msg.getType() == Message.Type.chat) {
								logger.debug(String.format(
										"[1:1 CHAT MESSAGE] participant : %s, local : %s",
										c.getParticipant(), locally));
								for (final ChatListener c : one2oneChatListeners) {
									c.onMessage(friend, msg.getBody());
								}
							}
						}
					});
				}
			}
		};
		
		ChatManager.getInstanceFor(connection)
					.addChatListener(one2oneListener);
	}
	
	private void installMucChatListener() {
		mucHandler = new MucHandle(this);
		MultiUserChat.addInvitationListener(connection, mucHandler);
	}
	
	static class MucHandle implements InvitationListener, PacketListener {
		Logger logger = LoggerFactory.getLogger(MucHandle.class);
		List<MucListener> listeners ;
		LolChat chatApi;
		public MucHandle(LolChat chat) {
			chatApi = chat;
			listeners = chat.mucChatListeners;
		}
		
		@Override
		public void invitationReceived(XMPPConnection conn, String room,
				String inviter, String reason, String password, Message message) {
			logger.debug (String.format("[INVITE]inviter:%s, room:%s,reason:%s, pass:%s, msg:%s", 
					inviter, room, reason, password, message));
			ArrayList<MucListener> cloned = new ArrayList<>(listeners);
			
			for( MucListener mL : cloned) {
				try {
					boolean acceptInvt = mL.invitationReceived(chatApi, room, inviter, password);	
					if ( acceptInvt) {
						MultiUserChat muc = new MultiUserChat(conn, room);
						muc.addMessageListener(this);
						muc.join(chatApi.getName(true));
						Message msg = muc.createMessage();
						msg.setBody("JOIN TO THE CHAT ROOM");
						muc.sendMessage(msg);
					}
				} catch (Exception e) {
					logger.error("unexpected exception", e);
				}
			}
		}

		@Override
		public void processPacket(Packet packet) throws NotConnectedException {
			// TODO Auto-generated method stub
			logger.debug(String.format("[PACKET] from : %s, \n        detail : %s", packet.getFrom(), packet.toString()));
			String room = packet.getFrom();
			String me = "sum33072235@pvp.net/xiff";
			
			ArrayList<MucListener> cloned = new ArrayList<>(listeners);
			for ( MucListener mL : cloned) {
				
//				mL.onMucMessage(room, )
			}
		}
	}

	/**
	 * Adds a ChatListener that listens to messages from all your friends.
	 * 
	 * @param chatListener
	 *            The ChatListener
	 */
	public void addChatListener(ChatListener chatListener) {
		one2oneChatListeners.add(chatListener);
	}
	
	/**
	 * Adds a multi-user-chat listener
	 * @param mucListener
	 */
	public void addMultiUserChatListener ( MucListener mucListener) {
		mucChatListeners.add(mucListener);
	}

	/**
	 * Adds a ConnectionListener that listens to connections, disconnections and
	 * reconnections.
	 * 
	 * @param conListener
	 *            The ConnectionListener
	 */
	public void addConnectionListener(ConnectionListener conListener) {
		connectionListeners.add(conListener);
	}

	/**
	 * Sends an friend request to an other user.
	 * 
	 * @param userId
	 *            The userId of the user you want to add (e.g.
	 *            sum12345678@pvp.net).
	 */
	public void addFriendById(String userId) {
		addFriendById(userId, null, getDefaultFriendGroup());
	}

	/**
	 * Sends an friend request to an other user.
	 * 
	 * @param userId
	 *            The userId of the user you want to add (e.g.
	 *            sum12345678@pvp.net).
	 * @param friendGroup
	 *            The FriendGroup you want to put this user in.
	 */
	public void addFriendById(String userId, FriendGroup friendGroup) {
		addFriendById(userId, null, friendGroup);
	}

	/**
	 * Sends an friend request to an other user.
	 * 
	 * @param userId
	 *            The userId of the user you want to add (e.g.
	 *            sum12345678@pvp.net).
	 * @param name
	 *            The name you want to assign to this user.
	 */
	public void addFriendById(String userId, String name) {
		addFriendById(userId, name, getDefaultFriendGroup());
	}

	/**
	 * Sends an friend request to an other user.
	 * 
	 * @param userId
	 *            The userId of the user you want to add (e.g.
	 *            sum12345678@pvp.net).
	 * @param name
	 *            The name you want to assign to this user.
	 * @param friendGroup
	 *            The FriendGroup you want to put this user in.
	 */
	public void addFriendById(String userId, String name,
			FriendGroup friendGroup) {
		if (name == null && getRiotApi() != null) {
			try {
				name = getRiotApi().getName(userId);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		try {
			connection
					.getRoster()
					.createEntry(
							StringUtils.parseBareAddress(userId),
							name,
							new String[] { friendGroup == null ? getDefaultFriendGroup()
									.getName() : friendGroup.getName() });
		} catch (NotLoggedInException | NoResponseException
				| XMPPErrorException | NotConnectedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends an friend request to an other user. An Riot API key is required for
	 * this.
	 * 
	 * @param name
	 *            The name of the Friend you want to add (case insensitive)
	 * @return True if succesful otherwise false.
	 */
	public boolean addFriendByName(String name) {
		return addFriendByName(name, getDefaultFriendGroup());
	}

	/**
	 * Sends an friend request to an other user. An Riot API key is required for
	 * this.
	 * 
	 * @param name
	 *            The name of the Friend you want to add (case insensitive)
	 * @param friendGroup
	 *            The FriendGroup you want to put this user in.
	 * @return True if succesful otherwise false.
	 */
	public boolean addFriendByName(String name, FriendGroup friendGroup) throws LolException{
		if (getRiotApi() != null) {
			try {
				final StringBuilder buf = new StringBuilder();
				buf.append("sum");
				buf.append(getRiotApi().getSummonerId(name));
				buf.append("@pvp.net");
				addFriendById(buf.toString(), name, friendGroup);
				return true;
			} catch ( RiotApiException e) {
				int rsCode = e.getResponseCode();
				throw new LolException("[" + rsCode + "] " + e.getMessage() + name, e);
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	public void removeFriend( Friend friend ) throws LolException{
		Roster roster = connection.getRoster();
		RosterEntry entry = roster.getEntry(friend.getUserId());
		try {
			roster.removeEntry(entry);
			friends.remove(friend);
		} catch (NotLoggedInException e) {
			throw new LolException("LOGIN REQUIRED", e);
		} catch (NoResponseException e) {
			throw new LolException("NO RESPONSE FROM SERVER", e);
		} catch (XMPPErrorException e) {
			throw new LolException("XMPP Error", e);
		} catch (NotConnectedException e) {
			throw new LolException("CONNECTION PROBLEM", e);
		}
	}

	/**
	 * Creates a new FriendGroup. If this FriendGroup contains no Friends when
	 * you logout it will be erased from the server.
	 * 
	 * @param name
	 *            The name of this FriendGroup
	 * @return The new FriendGroup or null if a FriendGroup with this name
	 *         already exists.
	 */
	public FriendGroup addFriendGroup(String name) {
		final RosterGroup g = connection.getRoster().createGroup(name);
		if (g != null) {
			return new FriendGroup(this, connection, g);
		}
		return null;
	}

	/**
	 * Adds a FriendListener that listens to changes from all your friends. Such
	 * as logging in, starting games, ...
	 * 
	 * @param friendListener
	 *            The FriendListener that you want to add
	 */
	public void addFriendListener(FriendListener friendListener) {
		friendListeners.add(friendListener);
	}

	
	

	/**
	 * Disconnects from chatserver and releases all resources.
	 */
	public void disconnect() {
		connection.getRoster().removeRosterListener(leagueRosterListener);
		try {
			connection.disconnect();
		} catch (final NotConnectedException e) {
			e.printStackTrace();
		}
//		stop = true;
	}

	/**
	 * Gets all ChatListeners that have been added.
	 * 
	 * @return List of ChatListeners
	 */
	public List<ChatListener> getChatListeners() {
		return one2oneChatListeners;
	}

	/**
	 * Gets the default FriendGroup.
	 * 
	 * @return Default FriendGroup
	 */
	public FriendGroup getDefaultFriendGroup() {
		return getFriendGroupByName(FriendGroup.DEFAULT_FRIENDGROUP);
	}

	/**
	 * Gets a friend based on a given filter.
	 * 
	 * @param filter
	 *            The filter defines conditions that your Friend must meet.
	 * @return The first Friend that meets the conditions or null if not found.
	 */
	public Friend getFriend(Filter<Friend> filter) {
		Collection<RosterEntry> entries = connection.getRoster().getEntries();
		for (final RosterEntry e : entries) {
			final Friend f = new Friend(this, connection, e);
			if (filter.accept(f)) {
				return f;
			}
		}
		return null;
	}

	/**
	 * Gets a friend based on his XMPPAddress.
	 * 
	 * @param xmppAddress
	 *            For example sum12345678@pvp.net
	 * @return The corresponding Friend or null if user is not found or he is
	 *         not a friend of you.
	 */
	public Friend getFriendById(String xmppAddress) {
		// finds a cached instance
		Iterator<Friend> itr = friends.iterator();
		Friend friend = null;
		while ( itr.hasNext()) {
			friend = itr.next();
			if ( friend.getUserId().equals ( xmppAddress)) {
				return friend;
			}
		}
			
		// COMMENT 친구가 없으면 재조회 하는게 맞나?
		//         프로그램 시작 시 서버에서 친구 목록을 전부 전송해주기 때문에
		//         다시 조회해도 없을 가능성이 높음.
		logger.debug(String.format("New Friend(%s) instance [T:%s]", xmppAddress, Thread.currentThread().getName()));
		final RosterEntry entry = connection.getRoster().getEntry(
				StringUtils.parseBareAddress(xmppAddress));
		if (entry == null) {
			logger.debug(String.format("fail to find friend : %s", xmppAddress));
		}
		friend = new Friend(this, connection, entry);
		friends.add(friend);
		return friend;
	}

	/**
	 * Gets a friend based on his name. The name is case insensitive. Beware:
	 * some names of Friends can be null unless an riot API Key is provided.
	 * 
	 * @param name
	 *            The name of your friend, for example "Dyrus"
	 * @return The corresponding Friend object or null if user is not found or
	 *         he is not a friend of you
	 */
	public Friend getFriendByName(final String name) {
		return getFriend(new Filter<Friend>() {

			public boolean accept(Friend friend) {
				return friend.getName() != null
						&& friend.getName().equalsIgnoreCase(name);
			}
		});
	}

	/**
	 * Gets a FriendGroup by name, for example "Duo Partners". The name is case
	 * sensitive! The FriendGroup will be created if it didn't exist yet.
	 * 
	 * @param name
	 *            The name of your group (case-sensitive)
	 * @return The corresponding FriendGroup
	 */
	public FriendGroup getFriendGroupByName(String name) {
		final RosterGroup g = connection.getRoster().getGroup(name);
		if (g != null) {
			return new FriendGroup(this, connection, g);
		}
		return addFriendGroup(name);
	}

	/**
	 * Get a list of all your FriendGroups.
	 * 
	 * @return A List of all your FriendGroups
	 */
	public List<FriendGroup> getFriendGroups() {
		final ArrayList<FriendGroup> groups = new ArrayList<>();
		for (final RosterGroup g : connection.getRoster().getGroups()) {
			groups.add(new FriendGroup(this, connection, g));
		}
		return groups;
	}

	/**
	 * Gets all FriendListeners that have been added.
	 * 
	 * @return List of FriendListeners
	 */
	public List<FriendListener> getFriendListeners() {
		return friendListeners;
	}

	/**
	 * Gets the current FriendRequestPolicy.
	 * 
	 * @return The current FriendRequestPolicy
	 */
	public FriendRequestPolicy getFriendRequestPolicy() {
		return friendRequestPolicy;
	}

	/**
	 * Get all your friends, both online and offline.
	 * 
	 * @return A List of all your Friends
	 */
	public List<Friend> getFriends() {
		return getFriends(new Filter<Friend>() {

			public boolean accept(Friend e) {
				return true;
			}
		});
	}

	/**
	 * Gets a list of your friends based on a given filter.
	 * 
	 * @param filter
	 *            The filter defines conditions that your Friends must meet.
	 * @return A List of your Friends that meet the condition of your Filter
	 */
	public List<Friend> getFriends(Filter<Friend> filter) {
		leagueRosterListener.waitForLoaded();
		final List<Friend> friends = new ArrayList<>();
		for (final Friend f : this.friends ) {
			if (filter.accept(f)) {
				friends.add(f);
			}
		}
		return friends;
	}

	/**
	 * Gets the LolStatus of the user that is logged in.
	 * 
	 * @return The current LolStatus.
	 * @see LolChat#setStatus(LolStatus)
	 */
	public LolStatus getLolStatus() {
		return status;
	}

	/**
	 * Gets the name of the user that is logged in. An Riot API key has to be
	 * provided.
	 * 
	 * @param forcedUpdate
	 *            True will force to update the name even when it is not null.
	 * @return The name of this user or null if something went wrong.
	 */
	public String getName(boolean forcedUpdate) throws RiotApiException {
		if ((name == null || forcedUpdate) && getRiotApi() != null) {
			try {
				name = getRiotApi().getName(connection.getUser());
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return name;
	}

	/**
	 * Get all your friends who are offline.
	 * 
	 * @return A list of all your offline Friends
	 */
	public List<Friend> getOfflineFriends() {
		return getFriends(new Filter<Friend>() {

			public boolean accept(Friend friend) {
				return !friend.isOnline();
			}
		});
	}

	/**
	 * Get all your friends who are online.
	 * 
	 * @return A list of all your online Friends
	 */
	public List<Friend> getOnlineFriends() {
		return getFriends(new Filter<Friend>() {

			public boolean accept(Friend friend) {
				return friend.isOnline();
			}
		});
	}

	/**
	 * Gets a list of user that you've sent friend requests but haven't answered
	 * yet.
	 * 
	 * @return A list of Friends.
	 */
	public List<Friend> getPendingFriendRequests() {
		return getFriends(new Filter<Friend>() {

			public boolean accept(Friend friend) {
				return friend.getFriendStatus() == FriendStatus.ADD_REQUEST_PENDING;
			}
		});
	}

	/**
	 * Gets the RiotApi used to resolve summonerId's and summoner names. Is null
	 * when no apiKey is provided or the region is not supported by the riot
	 * api.
	 * 
	 * @return The RiotApi object or null if no apiKey is provided or the region
	 *         is not supported by the riot api.
	 */
	public RiotApi getRiotApi() {
		return riotApi;
	}

	/**
	 * Returns true if currently connected to the XMPP server.
	 * 
	 * @return True if connected
	 */
	public boolean isConnected() {
		return connection.isConnected();
	}

	/**
	 * Returns true if server has sent us all information after logging in.
	 * 
	 * @return True if server has sent us all information after logging in,
	 *         otherwise false.
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * Returns true if your appearance is set to online, otherwise false.
	 * 
	 * @return True if your appearance is set to online, false if set to
	 *         offline.
	 */
	public boolean isOnline() {
		return type == Presence.Type.available;
	}

	/**
	 * <p>
	 * Logs in to the chat server without replacing the official connection of
	 * the League of Legends client.
	 * </p>
	 * 
	 * <p>
	 * Note: add/set all listeners before logging in, otherwise some offline
	 * messages can get lost.<br>
	 * Note: Garena servers use different credentials to log in.
	 * {@link GarenaLogin}
	 * </p>
	 * 
	 * @param username
	 *            Username of your account
	 * @param password
	 *            Password of your account
	 * @return true if login is successful, false otherwise
	 * @see GarenaLogin Logging in on Garena servers
	 */
	public boolean login(String username, String password) throws LolException{
		return login(username, password, false);
	}

	/**
	 * <p>
	 * Connects to the server and logs you in. If the server is unavailable then
	 * it will retry after a certain time period. It will not return unless the
	 * connection is successful.
	 * </p>
	 * 
	 * <p>
	 * Note: add/set all listeners before logging in, otherwise some offline
	 * messages can get lost.<br>
	 * Note: Garena servers use different credentials to log in.
	 * {@link GarenaLogin}
	 * </p>
	 * 
	 * @param username
	 *            Username of your account
	 * @param password
	 *            Password of your account
	 * @param replaceLeague
	 *            True will disconnect you account from the League of Legends
	 *            client. False allows you to have another connection open next
	 *            to the official connection in the League of Legends client.
	 * @return true if login was succesful, false otherwise
	 * @see GarenaLogin Logging in on Garena servers
	 */
	public boolean login(String username, String password, boolean replaceLeague) throws LolException{

		try {
			/*
			 *  smack쪽에서 socket 연결을 확립하고 reader, writer용 스레드를 생성함.
			 */
			connection.connect();
		} catch (SmackException | IOException | XMPPException cause) {
			throw new ConnectionException("[CONNECTION FAILED]", cause);
		}
		
		logger.debug("[CONNECTED] suucess to connect to lol server");
		// Login
		try {
			server.loginMethod.login(connection, username, password, replaceLeague);
			logger.debug("[LOGIN SUCCESS] login success");
			return true;
		} catch ( LoginException e) {
			logger.debug("[LOGIN FAILRUE]");
			try {
				connection.disconnect();
				logger.debug("[CONNECTION CLOSED]");
			} catch (NotConnectedException e1) {
				e1.printStackTrace();
			}
			throw e;
		}
	}
	
	public XMPPConnection login(Login loginInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Removes the ChatListener from the list and will no longer be called.
	 * 
	 * @param chatListener
	 *            The ChatListener that you want to remove
	 */
	public void removeChatListener(ChatListener chatListener) {
		one2oneChatListeners.remove(chatListener);
	}

	/**
	 * Removes the ConnectionListener from the list and will no longer be
	 * called.
	 * 
	 * @param conListener
	 *            The ConnectionListener that you want to remove
	 */
	public void removeConnectionListener(ConnectionListener conListener) {
		connectionListeners.remove(conListener);
	}

	/**
	 * Removes the FriendListener from the list and will no longer be called.
	 * 
	 * @param friendListener
	 *            The FriendListener that you want to remove
	 */
	public void removeFriendListener(FriendListener friendListener) {
		friendListeners.remove(friendListener);
	}

	/**
	 * Changes your ChatMode (e.g. busy, away, available).
	 * 
	 * @param chatMode
	 *            The new ChatMode
	 * @see ChatMode
	 */
	public void setChatMode(ChatMode chatMode) {
		this.mode = chatMode.mode;
		updateStatus();
	}

	/**
	 * Changes the current FriendRequestListener. It is recommended to do this
	 * before logging in.
	 * 
	 * @param friendRequestListener
	 *            The new FriendRequestListener
	 */
	public void setFriendRequestListener(
			FriendRequestListener friendRequestListener) {
		leaguePacketListener.setFriendRequestListener(friendRequestListener);
	}

	/**
	 * Changes the the current FriendRequestPolicy.
	 * 
	 * @param friendRequestPolicy
	 *            The new FriendRequestPolicy
	 * @see FriendRequestPolicy
	 */
	public void setFriendRequestPolicy(FriendRequestPolicy friendRequestPolicy) {
		this.friendRequestPolicy = friendRequestPolicy;
	}

	/**
	 * Change your appearance to offline.
	 * 
	 */
	public void setOffline() {
		invisible = true;
		updateStatus();
	}

	/**
	 * Change your appearance to online.
	 * 
	 */
	public void setOnline() {
		invisible = false;
		updateStatus();
	}

	/**
	 * Update your own status with current level, ranked wins...
	 * 
	 * Create an Status object (without constructor arguments) and call the
	 * several ".set" methods to customise it. After that pass this Status
	 * object back to this method.
	 * 
	 * @param status
	 *            Your custom Status object
	 * @see LolStatus
	 */
	public void setStatus(LolStatus status) {
		this.status = status;
		updateStatus();
	}

	private void updateStatus() {
		final CustomPresence newPresence = new CustomPresence(type,
				status.toString(), 1, mode);
		newPresence.setInvisible(invisible);
		try {
			connection.sendPacket(newPresence);
		} catch (final NotConnectedException e) {
			e.printStackTrace();
		}
	}

	public XMPPConnection getConnection() {
		return connection;
	}
	
	public ChatRoom joinPublicRoom(final String plainRoomName){
		return joinPublicRoom(plainRoomName, null);
	}
	
	public ChatRoom joinPublicRoom(String roomName, MucListener mucListener) {
		ChatRoom chatRoom = joinRoom(roomName, mucListener);
		return chatRoom;
	}
	
	/**
	 * 
	 * @param roomId - full qualified jabberID of a chat room
	 */
	ChatRoom joinRoom(final String roomName) throws MucException{
		return joinRoom(roomName, null);
	}
	
	ChatRoom joinRoom(final String roomName, MucListener listener) throws MucException{
		final String roomId = roomNaming.translate(roomName);
		logger.debug(String.format("[ROOM NAME] %s => %s", roomName, roomId));
		XMPPConnection conn = getConnection();
		MultiUserChat muc = new MultiUserChat(conn, roomId);
		
		try {
			ChatRoom room = new ChatRoom(this, muc, roomName);
			if ( listener != null ){
				room.addMucListener(listener);
			}
			
			muc.join(name);
			logger.debug(String.format("[MUC] joined to %s (nick:%s)", roomId, muc.getNickname()));
			return room;
			
		} catch (NoResponseException e) {
			throw new MucException("no response for joining a chat room : " + roomId , e);
		} catch (XMPPErrorException e) {
			throw new MucException("xmpp protocol error : " , e);
		} catch (NotConnectedException e) {
			throw new MucException("not connected.", e);
		}
	}
//	/**
//	 * 
//	 * @param roomName
//	 * @return
//	 */
//	public ChatRoom prepareChatRoom(String roomName) {
//		MultiUserChat muc = new MultiUserChat(connection, roomName);
//		ChatRoom room = new ChatRoom(muc);
//		return null;
//	}


}
