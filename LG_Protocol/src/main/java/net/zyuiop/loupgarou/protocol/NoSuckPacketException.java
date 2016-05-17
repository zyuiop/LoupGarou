package net.zyuiop.loupgarou.protocol;

/**
 * @author zyuiop
 */
public class NoSuckPacketException extends Exception {
	public NoSuckPacketException(int packetId) {
		super("No packet for id " + packetId + " (0x" + Integer.toHexString(packetId).toUpperCase() + ")");
	}

	public NoSuckPacketException(Class<? extends Packet> clazz) {
		super("No packet id for class " + clazz.getName());
	}
}
