package net.zyuiop.loupgarou.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.zyuiop.loupgarou.protocol.Packet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class ProtocolHandler extends ChannelInboundHandlerAdapter {
	private static Map<Class<? extends Packet>, PacketHandler<?>> handlerMap = new HashMap<>();
	private final NetworkManager manager;

	public ProtocolHandler(NetworkManager manager) {this.manager = manager;}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Packet packet = (Packet) msg;
		handle(packet);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		manager.disconnect();
	}

	private <T extends Packet> void handle(T packet) {
		try {
			PacketHandler<T> handler = (PacketHandler<T>) handlerMap.get(packet.getClass());
			if (handler != null)
				handler.handle(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static <T extends Packet> void handle(Class<T> clazz, PacketHandler<T> handler) {
		handlerMap.put(clazz, handler);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO : Dialog error
		cause.printStackTrace();
		ctx.close();
	}
}
