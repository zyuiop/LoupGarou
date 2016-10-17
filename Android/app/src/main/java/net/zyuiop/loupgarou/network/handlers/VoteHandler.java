package net.zyuiop.loupgarou.network.handlers;

import net.zyuiop.loupgarou.network.NetworkManager;
import net.zyuiop.loupgarou.network.PacketHandler;
import net.zyuiop.loupgarou.protocol.packets.clientbound.VoteRequestPacket;

/**
 * @author zyuiop
 */
public class VoteHandler implements PacketHandler<VoteRequestPacket> {
	private final NetworkManager manager;

	public VoteHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(VoteRequestPacket packet) {
		// Platform.runLater(() -> manager.getGameWindow().startVote(packet));
	}
}
