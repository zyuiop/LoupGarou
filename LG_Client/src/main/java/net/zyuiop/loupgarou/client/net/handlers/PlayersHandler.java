package net.zyuiop.loupgarou.client.net.handlers;

import net.zyuiop.loupgarou.client.net.PacketHandler;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetPlayersPacket;

/**
 * @author zyuiop
 */
public class PlayersHandler implements PacketHandler<SetPlayersPacket> {
	private final NetworkManager manager;

	public PlayersHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(SetPlayersPacket packet) {
		manager.getGameWindow().setPlayers(packet);
	}
}
