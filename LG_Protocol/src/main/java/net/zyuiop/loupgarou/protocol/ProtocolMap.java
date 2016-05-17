package net.zyuiop.loupgarou.protocol;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.zyuiop.loupgarou.game.GameState;
import net.zyuiop.loupgarou.protocol.packets.clientbound.*;
import net.zyuiop.loupgarou.protocol.packets.serverbound.CreateGamePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.JoinGamePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.LoginPacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.VotePacket;

/**
 * @author zyuiop
 */
public class ProtocolMap {
	public static int protocolVersion = 1;
	private static BiMap<Integer, Class<? extends Packet>> protocolMap = HashBiMap.create();

	static {
		protocolMap.put(0x00, LoginPacket.class);

		protocolMap.put(0x01, GameLeavePacket.class);
		protocolMap.put(0x02, GameListPacket.class);
		protocolMap.put(0x03, GameStatePacket.class);
		protocolMap.put(0x04, LoginResponsePacket.class);
		protocolMap.put(0x05, MessagePacket.class);
		protocolMap.put(0x06, SetPhasePacket.class);
		protocolMap.put(0x07, SetRolePacket.class);
		protocolMap.put(0x08, SetStatePacket.class);
		protocolMap.put(0x09, VoteEndPacket.class);
		protocolMap.put(0x0F, VoteRequestPacket.class);

		protocolMap.put(0xA1, CreateGamePacket.class);
		protocolMap.put(0xA2, JoinGamePacket.class);
		protocolMap.put(0xA3, VotePacket.class);
	}

	public static Class<? extends Packet> getPacketFor(int packetId) throws NoSuckPacketException {
		if (protocolMap.containsKey(packetId))
			return protocolMap.get(packetId);
		else
			throw new NoSuckPacketException(packetId);
	}

	public static int getPacketIdFor(Packet packet) throws NoSuckPacketException {
		Class<? extends Packet> packetClass = packet.getClass();
		if (protocolMap.inverse().containsKey(packetClass))
			return protocolMap.inverse().get(packetClass);
		else
			throw new NoSuckPacketException(packetClass);
	}
}
