package net.zyuiop.loupgarou.network.handlers;

import net.zyuiop.loupgarou.network.NetworkManager;
import net.zyuiop.loupgarou.network.PacketHandler;
import net.zyuiop.loupgarou.protocol.packets.clientbound.VoteEndPacket;

/**
 * @author zyuiop
 */
public class VoteEndHandler implements PacketHandler<VoteEndPacket> {
	private final NetworkManager manager;

	public VoteEndHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(VoteEndPacket packet) {
		//Platform.runLater(() -> manager.getGameWindow().finishVote(packet));
	}
}
