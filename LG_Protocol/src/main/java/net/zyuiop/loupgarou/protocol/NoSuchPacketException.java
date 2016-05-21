package net.zyuiop.loupgarou.protocol;

import java.io.IOException;

/**
 * @author zyuiop
 */
public class NoSuchPacketException extends IOException {
	public NoSuchPacketException(int packetId) {
		super("No packet for id " + packetId + " (0x" + Integer.toHexString(packetId).toUpperCase() + ")");
	}

	public NoSuchPacketException(Class<? extends Packet> clazz) {
		super("No packet id for class " + clazz.getName());
	}
}
