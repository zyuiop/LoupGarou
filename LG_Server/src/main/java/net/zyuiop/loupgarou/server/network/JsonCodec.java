package net.zyuiop.loupgarou.server.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.server.utils.GsonManager;

import java.util.List;

/**
 * @author zyuiop
 */
public class JsonCodec extends MessageToMessageCodec<WebSocketFrame, Packet> {
	@Override
	protected void encode(ChannelHandlerContext context, Packet packet, List<Object> list) throws Exception {
		list.add(new TextWebSocketFrame(GsonManager.getGson().toJson(packet, Packet.class)));
	}

	@Override
	protected void decode(ChannelHandlerContext context, WebSocketFrame s, List<Object> list) throws Exception {
		if (!(s instanceof TextWebSocketFrame))
			throw new UnsupportedOperationException("Unsupported frame type " + s.getClass().getName());

		list.add(GsonManager.getGson().fromJson(((TextWebSocketFrame) s).text(), Packet.class));
	}
}
