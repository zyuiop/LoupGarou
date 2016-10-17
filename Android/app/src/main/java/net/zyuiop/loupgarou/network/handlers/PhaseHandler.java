package net.zyuiop.loupgarou.network.handlers;

import net.zyuiop.loupgarou.network.NetworkManager;
import net.zyuiop.loupgarou.network.PacketHandler;
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
		// Platform.runLater(() -> manager.getGameWindow().setPhase(packet.getPhase()));
	}
}
