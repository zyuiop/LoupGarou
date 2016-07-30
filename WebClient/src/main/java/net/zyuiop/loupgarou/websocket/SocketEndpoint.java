package net.zyuiop.loupgarou.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import com.google.gson.Gson;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.websocket.net.NetworkManager;
import net.zyuiop.loupgarou.websocket.webprotocol.ConnectPacket;
import net.zyuiop.loupgarou.websocket.webprotocol.GsonManager;

/**
 * Created by zyuiop on 30/07/2016.
 * Part of the lg-parent project.
 */
@ServerEndpoint(value = "/game")
public class SocketEndpoint {
	private static Map<String, NetworkManager> registeredSessions = new HashMap<>();

	@OnOpen
	public void open(Session session) {
		SocketServer.logger.info("Session connected : " + session.getId());
	}

	@OnClose
	public void close(Session session) {
		NetworkManager manager = registeredSessions.remove(session.getId());
		if (manager != null)
			manager.stop();
	}

	@OnMessage
	public void message(Session session, String message) {
		if (!registeredSessions.containsKey(session.getId())) {
			ConnectPacket packet = GsonManager.getGson().fromJson(message, ConnectPacket.class);
			NetworkManager manager = new NetworkManager(packet.getName(), packet.getIp(), packet.getPort(), session);
			registeredSessions.put(session.getId(), manager);
			try {
				manager.connect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Packet packet = GsonManager.getGson().fromJson(message, Packet.class);
			registeredSessions.get(session.getId()).send(packet);
		}
	}

	@OnError
	public void error(Session session, Throwable error) {
		SocketServer.logger.log(Level.SEVERE, "Exception on session " + session.getId() + " : ", error);
		try {
			session.getBasicRemote().sendText(
					GsonManager.getGson().toJson(
							new MessagePacket(MessageType.ERROR, "An exception occured on your session."), Packet.class
					)
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
