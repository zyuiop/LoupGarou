package net.zyuiop.loupgarou.protocol.packets.clientbound;

import io.netty.buffer.ByteBuf;
import net.zyuiop.loupgarou.game.GameState;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;
import net.zyuiop.loupgarou.protocol.network.GameInfo;

/**
 * @author zyuiop
 */
public class GameListPacket extends Packet {
	private GameInfo[] games;

	public GameListPacket() {
	}

	public GameListPacket(GameInfo[] games) {
		this.games = games;
	}

	@Override
	public void read(PacketData byteBuf) {
		int amt = byteBuf.readInt();
		games = new GameInfo[amt];

		for (int i = 0; i < amt; i++) {
			int id = byteBuf.readInt();
			String name = byteBuf.readString();
			String host = byteBuf.readString();
			GameState state = byteBuf.readEnum(GameState.class);
			int players = byteBuf.readUnsignedByte();
			int maxPlayers = byteBuf.readUnsignedByte();

			games[i] = new GameInfo(id, name, host, state, players, maxPlayers);
		}
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeInt(games.length);
		for (GameInfo info : games) {
			byteBuf.writeInt(info.getId());
			byteBuf.writeString(info.getGameName());
			byteBuf.writeString(info.getHoster());
			byteBuf.writeEnum(info.getState());
			byteBuf.writeByte(info.getCurrentPlayers());
			byteBuf.writeByte(info.getMaxPlayers());
		}
	}

	public GameInfo[] getGames() {
		return games;
	}

	public void setGames(GameInfo[] games) {
		this.games = games;
	}
}
