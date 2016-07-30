package net.zyuiop.loupgarou.websocket.net;

import java.util.logging.Level;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.websocket.SocketServer;
import net.zyuiop.loupgarou.websocket.webprotocol.GsonManager;

/**
 * @author zyuiop
 */
public class ProtocolHandler extends ChannelInboundHandlerAdapter {
	private final NetworkManager manager;

	public ProtocolHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Packet packet = (Packet) msg;
		String converted = GsonManager.getGson().toJson(packet, Packet.class);
		manager.getSession().getBasicRemote().sendText(converted);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		manager.stop();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		manager.getLogger().log(Level.SEVERE, "An exception occured on channel : ", cause);

		if (manager.getSession().isOpen()) {
			String converted = GsonManager.getGson().toJson(new MessagePacket(MessageType.ERROR, "Une erreur réseau s'est produite : " + cause.getMessage()), Packet.class);
			manager.getSession().getBasicRemote().sendText(converted);
		}

		ctx.close();
	}
}
