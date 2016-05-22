package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.utils.MessageModifier;

/**
 * @author zyuiop
 */
public class MessagePacket extends Packet {
	private MessageType     type;
	private String          sender;
	private String          message;
	private boolean customStyle = false;
	private MessageModifier modifier;
	private short red = -1;
	private short green = -1;
	private short blue = -1;

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

	public MessagePacket(MessageType type, String message, MessageModifier modifier, short red, short green, short blue) {
		this.type = type;
		this.message = message;
		this.customStyle = true;
		this.modifier = modifier;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public MessagePacket(MessageType type, String sender, String message, MessageModifier modifier, short red, short green, short blue) {
		this.type = type;
		this.message = message;
		this.customStyle = true;
		this.modifier = modifier;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	@Override
	public void read(PacketData byteBuf) {
		type = MessageType.valueOf(byteBuf.readString());
		if (type == MessageType.USER)
			sender = byteBuf.readString();
		message = byteBuf.readString();
		customStyle = byteBuf.readBoolean();

		if (customStyle) {
			modifier = byteBuf.readEnum(MessageModifier.class);
			red = byteBuf.readUnsignedByte();
			green = byteBuf.readUnsignedByte();
			blue = byteBuf.readUnsignedByte();
		}
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeString(type.name());
		if (type == MessageType.USER)
			byteBuf.writeString(sender);
		byteBuf.writeString(message);
		byteBuf.writeBoolean(customStyle);

		if (customStyle) {
			byteBuf.writeEnum(modifier);
			byteBuf.writeByte(red);
			byteBuf.writeByte(green);
			byteBuf.writeByte(blue);
		}
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

	public MessageModifier getModifier() {
		return modifier;
	}

	public short getRed() {
		return red;
	}

	public short getGreen() {
		return green;
	}

	public short getBlue() {
		return blue;
	}

	public boolean isCustomStyle() {
		return customStyle;
	}
}
