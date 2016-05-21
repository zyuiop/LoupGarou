package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.data.GameState;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class SetStatePacket extends Packet {
	private GameState state;

	public SetStatePacket(GameState state) {
		this.state = state;
	}

	public SetStatePacket() {
	}

	public GameState getState() {
		return state;
	}

	@Override
	public void read(PacketData byteBuf) {
		state = byteBuf.readEnum(GameState.class);
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeEnum(state);
	}
}
