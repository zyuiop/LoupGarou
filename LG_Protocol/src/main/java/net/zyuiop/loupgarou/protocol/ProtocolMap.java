package net.zyuiop.loupgarou.protocol;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.zyuiop.loupgarou.protocol.packets.clientbound.*;
import net.zyuiop.loupgarou.protocol.packets.serverbound.*;

/**
 * @author zyuiop
 */
public class ProtocolMap {
	public static int protocolVersion = 7;
	private static BiMap<Integer, Class<? extends Packet>> protocolMap = HashBiMap.create();

	static {
		protocolMap.put(0x00, LoginPacket.class);

		protocolMap.put(0x01, GameLeavePacket.class);
		protocolMap.put(0x02, GameListPacket.class);
		protocolMap.put(0x03, GameJoinConfirmPacket.class);
		protocolMap.put(0x04, LoginResponsePacket.class);
		protocolMap.put(0x05, MessagePacket.class);
		protocolMap.put(0x06, SetPhasePacket.class);
		protocolMap.put(0x07, SetRolePacket.class);
		protocolMap.put(0x08, SetStatePacket.class);
		protocolMap.put(0x09, SetPlayersPacket.class);
		protocolMap.put(0x0A, VoteEndPacket.class);
		protocolMap.put(0x0B, VoteRequestPacket.class);
		protocolMap.put(0x0C, VoteValuePacket.class);
		protocolMap.put(0x0D, SetGameCompositionPacket.class);

		protocolMap.put(0xA1, CreateGamePacket.class);
		protocolMap.put(0xA2, JoinGamePacket.class);
		protocolMap.put(0xA3, VotePacket.class);
		protocolMap.put(0xA4, RefreshGameListPacket.class);
		protocolMap.put(0xA5, SendMessagePacket.class);
		protocolMap.put(0xA6, ChangeGameCompositionPacket.class);
	}

	public static Class<? extends Packet> getPacketFor(int packetId) throws NoSuchPacketException {
		if (protocolMap.containsKey(packetId))
			return protocolMap.get(packetId);
		else
			throw new NoSuchPacketException(packetId);
	}

	public static int getPacketIdFor(Packet packet) throws NoSuchPacketException {
		Class<? extends Packet> packetClass = packet.getClass();
		if (protocolMap.inverse().containsKey(packetClass))
			return protocolMap.inverse().get(packetClass);
		else
			throw new NoSuchPacketException(packetClass);
	}
}
