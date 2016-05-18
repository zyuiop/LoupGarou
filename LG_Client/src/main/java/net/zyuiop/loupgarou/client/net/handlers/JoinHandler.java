package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import net.zyuiop.loupgarou.client.net.PacketHandler;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameJoinConfirmPacket;

/**
 * @author zyuiop
 */
public class JoinHandler implements PacketHandler<GameJoinConfirmPacket> {
	private final NetworkManager manager;

	public JoinHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(GameJoinConfirmPacket packet) {
		Platform.runLater(() -> manager.joinGame(packet));
	}
}
