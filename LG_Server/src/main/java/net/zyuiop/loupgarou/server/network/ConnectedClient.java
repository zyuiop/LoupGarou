package net.zyuiop.loupgarou.server.network;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.GamesManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class ConnectedClient {
	private static final Map<String, ConnectedClient> clients = new HashMap<>();
	private final String name;
	private final ChannelHandlerContext context;
	private final GamePlayer player;

	public ConnectedClient(ChannelHandlerContext context, String name) {
		this.context = context;
		this.name = name;
		this.player = GamePlayer.getPlayer(name);
		clients.put(name, this);
		this.player.setClient(this);
	}

	public String getName() {
		return name;
	}

	public void unregister() {
		clients.remove(name);
		GamesManager.leaveGame(GamePlayer.getPlayer(name), false);
		GamePlayer.getPlayer(name).setClient(null);
		if (context.channel().isOpen())
			context.close();
	}

	public static ConnectedClient getClient(String name) {
		return clients.get(name);
	}

	public GamePlayer getPlayer() {
		return player;
	}

	public ChannelFuture sendPacket(Packet packet) {
		return context.writeAndFlush(packet);
	}
}
