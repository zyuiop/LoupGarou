package net.zyuiop.loupgarou.protocol;

import java.io.IOException;

/**
 * @author zyuiop
 */
public class BadPacketException extends IOException {
	public BadPacketException(int packetId) {
		super("No packet for id " + packetId + " (0x" + Integer.toHexString(packetId).toUpperCase() + ")");
	}

	public BadPacketException(Class<? extends Packet> clazz) {
		super("No packet id for class " + clazz.getName());
	}

	public BadPacketException(String s) {
		super(s);
	}
}
