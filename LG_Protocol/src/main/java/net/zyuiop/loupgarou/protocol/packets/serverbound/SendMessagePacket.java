package net.zyuiop.loupgarou.protocol.packets.serverbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class SendMessagePacket extends Packet {
	private String message;

	public SendMessagePacket(String message) {
		this.message = message;
	}

	public SendMessagePacket() {
	}

	@Override
	public void read(PacketData byteBuf) {
		message = byteBuf.readString();
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeString(message);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
