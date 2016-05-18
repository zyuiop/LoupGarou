package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import net.zyuiop.loupgarou.client.net.PacketHandler;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetRolePacket;

/**
 * @author zyuiop
 */
public class RoleHandler implements PacketHandler<SetRolePacket> {
	private final NetworkManager manager;

	public RoleHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(SetRolePacket packet) {
		Platform.runLater(() -> manager.getGameWindow().setRole(packet.getRole()));
	}
}
