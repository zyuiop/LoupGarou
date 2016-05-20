package net.zyuiop.loupgarou.client.net.handlers;

import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.client.net.PacketHandler;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetGameCompositionPacket;

/**
 * @author zyuiop
 */
public class GameCompositionHandler implements PacketHandler<SetGameCompositionPacket>  {
	private final NetworkManager manager;

	public GameCompositionHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(SetGameCompositionPacket packet) {
		manager.getGameWindow().changeComposition(packet.getRoles());
	}
}
