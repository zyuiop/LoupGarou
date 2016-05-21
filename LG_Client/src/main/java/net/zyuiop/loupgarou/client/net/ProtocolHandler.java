package net.zyuiop.loupgarou.client.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import net.zyuiop.loupgarou.client.LGClient;
import net.zyuiop.loupgarou.protocol.Packet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class ProtocolHandler extends ChannelInboundHandlerAdapter {
	private static Map<Class<? extends Packet>, PacketHandler<?>> handlerMap = new HashMap<>();

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Packet packet = (Packet) msg;
		handle(packet);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LGClient.getInstance().disconnect();
	}

	private <T extends Packet> void handle(T packet) {
		try {
			LGClient.logger.info("Handled packet " + packet.getClass());
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
		Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Une erreur s'est produite : \n" + cause.getMessage(), new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)).show());
		cause.printStackTrace();
		ctx.close();
	}
}
