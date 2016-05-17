package net.zyuiop.loupgarou.server.network;

import net.zyuiop.loupgarou.protocol.Packet;

/**
 * @author zyuiop
 */
public interface PacketHandler<T extends Packet> {
	void handle(T packet, ConnectedClient client);
}
