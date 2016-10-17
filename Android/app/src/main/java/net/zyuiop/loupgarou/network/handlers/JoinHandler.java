package net.zyuiop.loupgarou.network.handlers;

import net.zyuiop.loupgarou.network.NetworkManager;
import net.zyuiop.loupgarou.network.PacketHandler;
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
		// Platform.runLater(() -> manager.joinGame(packet));
	}
}
