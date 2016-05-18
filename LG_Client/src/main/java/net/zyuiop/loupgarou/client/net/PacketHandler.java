package net.zyuiop.loupgarou.client.net;

import net.zyuiop.loupgarou.protocol.Packet;

/**
 * @author zyuiop
 */
public interface PacketHandler<T extends Packet> {
	void handle(T packet);
}
