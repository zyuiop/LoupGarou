package net.zyuiop.loupgarou.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.zyuiop.loupgarou.protocol.ProtocolMap;
import net.zyuiop.loupgarou.protocol.network.PacketDecoder;
import net.zyuiop.loupgarou.protocol.network.PacketEncoder;
import net.zyuiop.loupgarou.server.auth.AuthenticationService;
import net.zyuiop.loupgarou.server.auth.RSAAuthenticationService;
import net.zyuiop.loupgarou.server.game.GamesManager;
import net.zyuiop.loupgarou.server.network.JsonCodec;
import net.zyuiop.loupgarou.server.network.ProtocolHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.security.NoSuchAlgorithmException;

/**
 * @author zyuiop
 */
public class LGServer {
	private static LGServer instance;
	private static final Logger logger = LogManager.getRootLogger();
	private final int port;
	private final int wsPort;
	private AuthenticationService authenticationService;


	LGServer() throws NoSuchAlgorithmException {
		instance = this;
		this.port = 2325;
		this.wsPort = 8005;

		// TODO read conf
		logger.info("Starting LGServer on port " + port + " with protocol " + ProtocolMap.protocolVersion + "...");
		authenticationService = new RSAAuthenticationService(new File(System.getProperty("user.dir") + "/users/"));
		GamesManager.init();

		try {
			run();
		} catch (Exception e) {
			logger.catching(Level.ERROR, e);
		}
	}

	private ChannelFuture setupMainServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws InterruptedException {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new ProtocolHandler());
					}
				})
				.option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true);

		// Bind and start to accept incoming connections.
		return b.bind(port).sync(); //
	}

	private ChannelFuture setupWebSocket(EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws InterruptedException {
		ServerBootstrap b = new ServerBootstrap(); // (2)
		b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class) // (3)
				.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast(new HttpServerCodec());
						pipeline.addLast(new HttpObjectAggregator(65536));
						// pipeline.addLast(new WebSocketServerCompressionHandler());
						pipeline.addLast(new WebSocketServerProtocolHandler("/game"));
						pipeline.addLast(new JsonCodec());
						pipeline.addLast(new ProtocolHandler());
					}
				})
				.option(ChannelOption.SO_BACKLOG, 128)          // (5)
				.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

		// Bind and start to accept incoming connections.
		logger.info("Accepting connexions on websocket protocol on port " + wsPort);
		return b.bind(wsPort).sync(); // (7)
	}

	private void run() throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		EventLoopGroup ws_bossGroup = new NioEventLoopGroup(); // (1)
		EventLoopGroup ws_workerGroup = new NioEventLoopGroup();
		try {
			ChannelFuture main = setupMainServer(bossGroup, workerGroup);
			ChannelFuture ws = setupWebSocket(ws_bossGroup, ws_workerGroup);

			main.channel().closeFuture().sync();
			ws.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
			ws_workerGroup.shutdownGracefully();
			ws_bossGroup.shutdownGracefully();
		}
	}

	public static Logger getLogger() {
		return logger;
	}

	public static LGServer getInstance() {
		return instance;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}
}
