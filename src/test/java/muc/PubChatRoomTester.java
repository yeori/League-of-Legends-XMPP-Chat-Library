package muc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.net.ssl.SSLSocketFactory;

import muc.MultiChatTester.FriendHandler;
import muc.MultiChatTester.LolChatHandler;
import muc.MultiChatTester.MucHandler;
import muc.MultiChatTester.Sender;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.lolchatapi.ChatServer;
import com.github.theholywaffle.lolchatapi.FriendRequestPolicy;
import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiKey;
import com.github.yeori.lol.riotapi.DefaultRiotApiFactory;

public class PubChatRoomTester {
	static Logger logger = LoggerFactory.getLogger(MultiChatTester.class);
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
		api.addMultiUserChatListener(new MucHandler());
		if ( api.login(username, password, true) ) {
			
			api.addChatListener(new LolChatHandler());
			XMPPConnection conn = api.getConnection();
			new Sender(api ).startThread();
			
			joinTheRoom(api, "lol");
		} else {
			logger.error("fail to login");
		}
	}
	
	private static void joinTheRoom(LolChat api, String roomName) {
		// TODO Auto-generated method stub
//		String encRoomName = "pu~" + "403926033d001b5279df37cbbe5287b7c7c267fa";// lol 방
//		String encRoomName = "pu~" + "22a7dafc0309c94227145e2d077da24c8816645e";// 듀오 방
		String encRoomName = "pu~" + "aeab28925114716138085003c23bde7d2e75d094";// 아리 방
		
		String roomId = encRoomName + "@lvl.pvp.net";
		XMPPConnection conn = api.getConnection();
		MultiUserChat muc = new MultiUserChat(conn, roomId);
		try {
			String name = api.getName(true);
			muc.join(name);
			muc.addMessageListener(new MessageHandle());
			
			muc.addParticipantListener(new PacketListener() {
				
				@Override
				public void processPacket(Packet packet) throws NotConnectedException {
					Presence psc = Presence.class.cast(packet);
					logger.debug(String.format("[참여자] %s(%s)", psc.getFrom(), psc.getType()) );
					
				}
			});
			Message msg = muc.createMessage();
			msg.setBody("JOIN TO THE CHAT ROOM");
			muc.sendMessage(msg);
			RoomInfo roomInfo = MultiUserChat.getRoomInfo(conn, roomId);
			
			logger.debug("참여자 수 : " + roomInfo.getOccupantsCount());
			
		} catch (NoResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static class MessageHandle implements PacketListener{

		@Override
		public void processPacket(Packet packet) throws NotConnectedException {
			logger.debug("[PACKET FROM PUB CHAT]" + packet.toXML().toString());
		}
		
	}

}
