package net.zyuiop.loupgarou.server.network;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;

/**
 * @author zyuiop
 */
public class JsonProtocolHandler extends ProtocolHandler {
	@Override
	protected void loginRefuse(ChannelHandlerContext ctx, String reason) {
		ctx.writeAndFlush(new CloseWebSocketFrame(4000, "Connexion impossible : " + reason))
				.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						future.channel().close();
					}
				});
	}
}
