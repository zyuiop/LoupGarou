package net.zyuiop.loupgarou.client.net.handlers;

import com.google.common.collect.Lists;
import net.zyuiop.loupgarou.client.PacketHandler;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameListPacket;

/**
 * @author zyuiop
 */
public class GameListHandler implements PacketHandler<GameListPacket> {
	private final NetworkManager manager;

	public GameListHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(GameListPacket packet) {
		manager.getHomeWindow().setContent(Lists.newArrayList(packet.getGames()));
	}
}
