package net.zyuiop.loupgarou.protocol.packets.serverbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class JoinGamePacket extends Packet {
	private int gameId;

	public JoinGamePacket() {
	}

	public JoinGamePacket(int gameId) {
		this.gameId = gameId;
	}

	@Override
	public void read(PacketData byteBuf) {
		gameId = byteBuf.readInt();
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeInt(gameId);
	}

	public int getGameId() {
		return gameId;
	}

}
