package net.zyuiop.loupgarou.protocol.packets.serverbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class JoinGamePacket extends Packet {
	private int gameId;
	private String password = null;

	public JoinGamePacket() {
	}

	public JoinGamePacket(int gameId) {
		this.gameId = gameId;
	}

	public JoinGamePacket(int gameId, String password) {
		this.gameId = gameId;
		this.password = password;
	}

	@Override
	public void read(PacketData byteBuf) {
		gameId = byteBuf.readInt();
		password = byteBuf.readString();
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeInt(gameId);
		byteBuf.writeString(password);
	}

	public int getGameId() {
		return gameId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
