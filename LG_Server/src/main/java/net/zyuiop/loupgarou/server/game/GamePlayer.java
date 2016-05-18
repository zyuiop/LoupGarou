package net.zyuiop.loupgarou.server.game;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetRolePacket;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.roledata.RoleData;
import net.zyuiop.loupgarou.server.game.roledata.RoleDatas;
import net.zyuiop.loupgarou.server.network.ConnectedClient;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * @author zyuiop
 */
public class GamePlayer {
	private static final Map<String, GamePlayer> players = new HashMap<>();

	public static GamePlayer getPlayer(String name) {
		if (!players.containsKey(name))
			players.put(name, new GamePlayer(name));
		return players.get(name);
	}

	public static boolean hasPlayer(String name) {
		return players.containsKey(name);
	}

	private       ConnectedClient client;
	private       RoleData        role;
	private final String          name;
	private GamePlayer lover = null;
	private Game game;
	private Map<String, Object> attributes = new HashMap<>();
	private Queue<Packet> packetQueue = new ArrayDeque<>(); // Bad idea ?

	private GamePlayer(String name) {
		this.name = name;
	}

	public ConnectedClient getClient() {
		return client;
	} // TODO set private

	public void setClient(ConnectedClient client) {
		this.client = client;
		while (packetQueue.size() > 0)
			sendPacket(packetQueue.poll());
	}

	public Role getRole() {
		return role == null ? null : role.getRole();
	}

	public <T extends RoleData> T getRoleData(Class<T> tClass) {
		if (!tClass.isInstance(role))
			return null;
		return (T) role;
	}

	public void setRole(Role role) {
		this.role = RoleDatas.create(role);
		sendPacket(new MessagePacket(MessageType.GAME, "Vous jouez désormais " + role.getName() + " ! Votre objectif est : " + role.getObjective() + ". Vous disposez du pouvoir suivant : " + role.getPower()));
		sendPacket(new SetRolePacket(role));
		LGServer.getLogger().info("Attributed role " + role +" to " + getName());
	}

	public String getName() {
		return name;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
		attributes.clear();
	}

	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public void sendPacket(Packet packet) {
		if (client != null) {
			client.sendPacket(packet);
		} else {
			packetQueue.add(packet);
		}
	}

	public GamePlayer getLover() {
		return lover;
	}

	public void sendMessage(MessageType type, String message) {
		sendPacket(new MessagePacket(type, message));
	}

	public void setLover(GamePlayer lover) {
		this.lover = lover;
		if (lover != null)
			sendPacket(new MessagePacket(MessageType.GAME, "Cupidon vous a fait amoureux de " + lover.getName() + " !"));
	}
}
