package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.game.GamePhase;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class SetPhasePacket extends Packet {
	private GamePhase phase;

	public SetPhasePacket(GamePhase phase) {
		this.phase = phase;
	}

	public SetPhasePacket() {
	}

	public GamePhase getPhase() {
		return phase;
	}

	@Override
	public void read(PacketData byteBuf) {
		phase = byteBuf.readEnum(GamePhase.class);
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeEnum(phase);
	}
}
