package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import net.zyuiop.loupgarou.client.net.PacketHandler;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetPhasePacket;

/**
 * @author zyuiop
 */
public class PhaseHandler implements PacketHandler<SetPhasePacket> {
	private final NetworkManager manager;

	public PhaseHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(SetPhasePacket packet) {
		Platform.runLater(() -> manager.getGameWindow().setPhase(packet.getPhase()));
	}
}
