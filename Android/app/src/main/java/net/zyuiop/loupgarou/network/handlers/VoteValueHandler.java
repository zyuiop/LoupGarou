package net.zyuiop.loupgarou.network.handlers;

import net.zyuiop.loupgarou.network.NetworkManager;
import net.zyuiop.loupgarou.network.PacketHandler;
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
		// networkManager.getGameWindow().voteValue(packet);
	}
}
