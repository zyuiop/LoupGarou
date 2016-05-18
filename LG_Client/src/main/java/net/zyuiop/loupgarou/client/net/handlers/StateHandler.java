package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import net.zyuiop.loupgarou.client.net.PacketHandler;
import net.zyuiop.loupgarou.client.net.NetworkManager;
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
		Platform.runLater(() -> manager.getGameWindow().setState(packet.getState()));
	}
}
