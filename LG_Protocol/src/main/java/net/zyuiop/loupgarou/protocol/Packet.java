package net.zyuiop.loupgarou.protocol;

/**
 * @author zyuiop
 */
public abstract class Packet {
	public abstract void read(PacketData byteBuf);

	public abstract void write(PacketData byteBuf);
}
