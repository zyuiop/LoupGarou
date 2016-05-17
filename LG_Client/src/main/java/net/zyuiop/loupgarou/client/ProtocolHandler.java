package net.zyuiop.loupgarou.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.packets.clientbound.LoginResponsePacket;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class ProtocolHandler extends ChannelInboundHandlerAdapter {
	private static Map<Class<? extends Packet>, PacketHandler<?>> handlerMap = new HashMap<>();

	static {
		handle(LoginResponsePacket.class, packet -> {
			if (packet.isSuccess()) {
				BasicClient.logger.info("Login success : " + packet.getErrorMessage());
			} else {
				BasicClient.logger.info("Login failed : " + packet.getErrorMessage());
				System.exit(0);
			}
		});
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		BasicClient.logger.info("Disconnected.");
		System.exit(0);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Packet packet = (Packet) msg;
		handle(packet);
	}

	private <T extends Packet> void handle(T packet) {
		PacketHandler<T> handler = (PacketHandler<T>) handlerMap.get(packet.getClass());
		if (handler != null)
			handler.handle(packet);
	}

	public static <T extends Packet> void handle(Class<T> clazz, PacketHandler<T> handler) {
		handlerMap.put(clazz, handler);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
