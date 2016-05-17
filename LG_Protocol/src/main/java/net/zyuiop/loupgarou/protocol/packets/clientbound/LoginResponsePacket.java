package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class LoginResponsePacket extends Packet {
	private boolean success;
	private String  errorMessage;

	public LoginResponsePacket() {
	}

	public LoginResponsePacket(boolean success, String errorMessage) {
		this.success = success;
		this.errorMessage = errorMessage;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void read(PacketData byteBuf) {
		success = byteBuf.readBoolean();
		errorMessage = byteBuf.readString();
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeBoolean(success);
		byteBuf.writeString(errorMessage);
	}
}
