package net.zyuiop.loupgarou.network.handlers;

import net.zyuiop.loupgarou.network.NetworkManager;
import net.zyuiop.loupgarou.network.PacketHandler;
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
		//Platform.runLater(() -> manager.getGameWindow().setRole(packet.getRole()));
	}
}
