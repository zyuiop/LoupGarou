package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class GameLeavePacket extends Packet {
	private String reason;

	public GameLeavePacket() {
	}

	public GameLeavePacket(String reason) {
		this.reason = reason;
	}

	@Override
	public void read(PacketData byteBuf) {
		reason = byteBuf.readString();
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeString(reason);
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
