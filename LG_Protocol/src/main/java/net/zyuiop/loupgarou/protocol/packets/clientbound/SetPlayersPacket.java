package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class SetPlayersPacket extends Packet {
	private int      maxPlayers;
	private String[] players;

	public SetPlayersPacket() {
	}

	public SetPlayersPacket(int maxPlayers, String[] players) {
		this.maxPlayers = maxPlayers;
		this.players = players;
	}

	@Override
	public void read(PacketData byteBuf) {
		maxPlayers = byteBuf.readInt();
		players = byteBuf.readArray(String.class, byteBuf::readString);
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeInt(maxPlayers);
		byteBuf.writeArray(players, byteBuf::writeString);
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public String[] getPlayers() {
		return players;
	}
}
