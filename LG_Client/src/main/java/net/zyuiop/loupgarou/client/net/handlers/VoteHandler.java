package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.client.net.PacketHandler;
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
		Platform.runLater(() -> manager.getGameWindow().startVote(packet));
	}
}
