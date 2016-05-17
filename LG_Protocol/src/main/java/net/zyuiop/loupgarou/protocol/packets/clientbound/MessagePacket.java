package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;
import net.zyuiop.loupgarou.protocol.network.MessageType;

/**
 * @author zyuiop
 */
public class MessagePacket extends Packet {
	private MessageType type;
	private String      sender;
	private String      message;

	public MessagePacket() {
	}

	public MessagePacket(MessageType type, String sender, String message) {
		this.type = type;
		this.sender = sender;
		this.message = message;
	}

	public MessagePacket(MessageType type, String message) {
		this.type = type;
		this.message = message;
	}

	@Override
	public void read(PacketData byteBuf) {
		type = MessageType.valueOf(byteBuf.readString());
		if (type == MessageType.USER)
			sender = byteBuf.readString();
		message = byteBuf.readString();
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeString(type.name());
		if (type == MessageType.USER)
			byteBuf.writeString(sender);
		byteBuf.writeString(message);
	}

	public MessageType getType() {
		return type;
	}

	public String getSender() {
		return sender;
	}

	public String getMessage() {
		return message;
	}

}
