package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.client.net.PacketHandler;
import net.zyuiop.loupgarou.protocol.packets.clientbound.VoteEndPacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.VotePacket;

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
		Platform.runLater(() -> manager.getGameWindow().finishVote(packet));
	}
}
