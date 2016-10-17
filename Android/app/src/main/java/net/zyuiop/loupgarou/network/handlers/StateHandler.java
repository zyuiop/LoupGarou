package net.zyuiop.loupgarou.network.handlers;

import net.zyuiop.loupgarou.network.NetworkManager;
import net.zyuiop.loupgarou.network.PacketHandler;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetStatePacket;

/**
 * @author zyuiop
 */
public class StateHandler implements PacketHandler<SetStatePacket> {
	private final NetworkManager manager;

	public StateHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(SetStatePacket packet) {
		//Platform.runLater(() -> manager.getGameWindow().setState(packet.getState()));
	}
}
