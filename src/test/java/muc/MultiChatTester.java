package muc;



import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.lolchatapi.ChatServer;
import com.github.theholywaffle.lolchatapi.FriendRequestPolicy;
import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.LolStatus;
import com.github.theholywaffle.lolchatapi.listeners.ChatListener;
import com.github.theholywaffle.lolchatapi.listeners.FriendListener;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiKey;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;
import com.github.yeori.lol.riotapi.DefaultRiotApiFactory;

public class MultiChatTester {
	static Logger logger = LoggerFactory.getLogger(MultiChatTester.class);
	public MultiChatTester() {
		
	}

	public static void main(String[] args) throws IOException {
		InputStream is = MultiChatTester.class.getResourceAsStream("/accounts.properties");
		Properties prop = new Properties();
		prop.load(is);
		is.close();
		String username = prop.getProperty("gamja.id");
		String password = prop.getProperty("gamja.pass");
		String key      = prop.getProperty("gamja.key");
		
		logger.debug("starting ...");
		LolChat api = new LolChat( ChatServer.KR, 
				FriendRequestPolicy.MANUAL,
				new RiotApiKey(key), 
				SSLSocketFactory.getDefault(),
				new DefaultRiotApiFactory());
		api.addFriendListener(new FriendHandler());
		if ( api.login(username, password, true) ) {
			
			api.addChatListener(new LolChatHandler());
			XMPPConnection conn = api.getConnection();
//			TestPacketFilter filter = new TestPacketFilter();
//			
//			conn.addPacketListener(new IncomingPacketListener(), filter);
//			conn.addPacketSendingListener(new OutgoingPacketListener(), filter);
//			MultiUserChat.addInvitationListener(conn, new InvitationHandler());
			new Sender(api ).startThread();
		} else {
			logger.error("fail to login");
		}
	}
	
	static class FriendHandler implements FriendListener{

		@Override
		public void onFriendAvailable(Friend friend) {
			logger.info(String.format("[%s][AVAILABLE] 대화 가능",	friend.getName()));
			LolStatus ls = friend.getStatus();
			logger.debug(String.format("[MOBILE:%s]", ls.getMobile()));
			logger.debug(String.format("[status:%s]", ls.getStatusMessage()));
		}

		@Override
		public void onFriendAway(Friend friend) {
			logger.info(String.format("[%s][AWAY] 로그아웃했음", friend.getName()));
			
		}

		@Override
		public void onFriendBusy(Friend friend) {
			logger.info(String.format("[%s][BUSY] 바쁨으로 상태변경", friend.getName()));
		}

		@Override
		public void onFriendJoin(Friend friend) {
			logger.info(String.format("[%s][JOIN] 로그인했음", friend.getName()));
			
		}

		@Override
		public void onFriendLeave(Friend friend) {
			logger.info(String.format("[%s][LEAVE] 나갔음", friend.getName()));
			
		}

		@Override
		public void onFriendStatusChange(Friend friend) {
			logger.info(String.format("[%s][STATUS CHANGED] 상태변경", friend.getName()));
			
			
		}

		@Override
		public void onNewFriend(Friend friend) {
			logger.info(String.format("[%s][NEW] 새친구", friend.getName()));
			
		}

		@Override
		public void onRemoveFriend(String userId, String name) {
			logger.info(String.format("[ID:%s, NAME:%s][REMOVED] 친구 삭제",userId, name));
		}
	}
	
	static class LolChatHandler implements ChatListener {

		@Override
		public void onMessage(Friend friend, String message) {
			logger.debug(String.format("[%s]%s", friend.getName(), message));
		}
		
	}
	static class OutgoingPacketListener implements PacketListener {

		@Override
		public void processPacket(Packet packet) throws NotConnectedException {
			String from = packet.getFrom();
			String to = packet.getTo();
			String id = packet.getPacketID();
			XMPPError error = packet.getError();
			
			String fqClassName = packet.getClass().getName();
			if ( error != null ) {
				logger.debug("error", error);
			}
			logger.info("[OUT] " + packet.toXML().toString());
		}
	}
	
	static class IncomingPacketListener implements PacketListener {

		@Override
		public void processPacket(Packet packet) throws NotConnectedException {
			String from = packet.getFrom();
			String to = packet.getTo();
			String id = packet.getPacketID();
			XMPPError error = packet.getError();
			
			String fqClassName = packet.getClass().getName();
//			logger.debug(String.format("[type]%s", fqClassName.substring(fqClassName.lastIndexOf('.')+1)));
			if ( error != null ) {
				logger.debug("error", error);
			}
			logger.info("[IN ] "+ packet.toXML().toString());
		}
	}
	
	static class InvitationHandler implements InvitationListener {

		@Override
		public void invitationReceived(XMPPConnection conn, String room,
				String inviter, String reason, String password, Message message) {
			String nl = "\n        ";
			StringBuilder buf = new StringBuilder(nl);
			buf.append(String.format("ROOM   [%s]", room) + nl);
			buf.append(String.format("inviter[%s]", inviter) + nl);
			buf.append(String.format("reason [%s]", reason) + nl);
			buf.append(String.format("pass   [%s]", password) + nl);
			buf.append(String.format("message[%s]", message));
			
			logger.debug(buf.toString());
			
//			Presence joinPresence = new Presence(Type.available);
//			joinPresence.setTo(room);
			
			MultiUserChat muc = new MultiUserChat(conn, room);
			try {
				muc.join("감자깡이시다");
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					muc.sendMessage("나 왔다.");
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				conn.sendPacket(joinPresence);
				
			} catch (NotConnectedException | NoResponseException | XMPPErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}
		
	}
	static class Sender implements Runnable {
		static int packetId = 1000;
		XMPPConnection conn ;
		Roster roster ;
		String yangpa = "sum33172247@pvp.net/xiff";
		Thread thisThread ;
		LolChat chatApi;
		Sender ( LolChat chat) {
			this.chatApi = chat;
			this.conn = chat.getConnection();
			this.roster = conn.getRoster() ;
		}
		public void startThread() {
			thisThread = new Thread(this);
			thisThread.setName("T-SENDER");
			thisThread.start();
		}
		@Override
		public void run() {
			
			Scanner sc = new Scanner(System.in);
			
			
			String cmd = null;
			while ( true ){
				cmd = sc.nextLine();
				if ( "q".equals(cmd) ) {
					break;
				} else {
					Message msg = new Message(yangpa, Message.Type.chat);
					msg.setPacketID("sodkdkd" + nextId());
					try {
						msg.setBody(cmd);
						msg.setLanguage("ko");
						this.conn.sendPacket(msg);
					} catch (NotConnectedException e) {
						e.printStackTrace();
					}
				}
			}
			
			chatApi.disconnect();

			logger.debug("bye");
		}
		private int nextId() {
			return packetId++;
		}
	}
	

	
	static class TestPacketFilter implements PacketFilter {

		@Override
		public boolean accept(Packet packet) {
			
			return true;
		}
		
	}
}
