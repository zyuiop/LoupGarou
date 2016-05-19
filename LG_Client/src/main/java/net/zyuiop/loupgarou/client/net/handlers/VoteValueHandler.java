package net.zyuiop.loupgarou.client.net.handlers;

import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.client.net.PacketHandler;
import net.zyuiop.loupgarou.protocol.packets.clientbound.VoteValuePacket;

/**
 * @author zyuiop
 */
public class VoteValueHandler implements PacketHandler<VoteValuePacket> {
	private final NetworkManager networkManager;

	public VoteValueHandler(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	public void handle(VoteValuePacket packet) {
		networkManager.getGameWindow().voteValue(packet);
	}
}
