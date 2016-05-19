package net.zyuiop.loupgarou.protocol.packets.serverbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;
import net.zyuiop.loupgarou.protocol.ProtocolMap;

/**
 * @author zyuiop
 */
public class LoginPacket extends Packet {
	private int    protocolVersion;
	private String username;
	private boolean enforceAuth = false;
	private long   timestamp;
	private String publicKey;
	private byte[] signature;

	public LoginPacket() {
	}

	public LoginPacket(String username) {
		this.username = username;
	}

	public LoginPacket(String username, long timestamp, String publicKey, byte[] signature) {
		this.username = username;
		this.enforceAuth = true;
		this.timestamp = timestamp;
		this.publicKey = publicKey;
		this.signature = signature;
	}

	@Override
	public void read(PacketData byteBuf) {
		protocolVersion = byteBuf.readShort();
		username = byteBuf.readString();
		enforceAuth = byteBuf.readBoolean();
		if (enforceAuth) {
			timestamp = byteBuf.readLong();
			publicKey = byteBuf.readString();
			signature = new byte[byteBuf.readInt()];
			byteBuf.readBytes(signature);
		}
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeShort(ProtocolMap.protocolVersion);
		byteBuf.writeString(username);
		byteBuf.writeBoolean(enforceAuth);
		if (enforceAuth) {
			byteBuf.writeLong(timestamp);
			byteBuf.writeString(publicKey);
			byteBuf.writeInt(signature.length);
			byteBuf.writeBytes(signature);
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isEnforceAuth() {
		return enforceAuth;
	}

	public void setEnforceAuth(boolean enforceAuth) {
		this.enforceAuth = enforceAuth;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}
}
