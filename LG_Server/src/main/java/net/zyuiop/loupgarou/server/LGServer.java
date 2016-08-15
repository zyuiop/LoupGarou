package net.zyuiop.loupgarou.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import net.zyuiop.loupgarou.protocol.ProtocolMap;
import net.zyuiop.loupgarou.protocol.network.PacketDecoder;
import net.zyuiop.loupgarou.protocol.network.PacketEncoder;
import net.zyuiop.loupgarou.server.auth.AuthenticationService;
import net.zyuiop.loupgarou.server.auth.RSAAuthenticationService;
import net.zyuiop.loupgarou.server.game.GamesManager;
import net.zyuiop.loupgarou.server.network.JsonCodec;
import net.zyuiop.loupgarou.server.network.JsonProtocolHandler;
import net.zyuiop.loupgarou.server.network.ProtocolHandler;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.SyslogAppender;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * @author zyuiop
 */
public class LGServer {
	private static LGServer instance;
	private static final Logger logger = LogManager.getRootLogger();
	private final int port;
	private final int wsPort;
	private final boolean wsEnable;
	private AuthenticationService authenticationService;
	private SslContext sslContext;

	LGServer() throws NoSuchAlgorithmException {
		instance = this;

		int port = 2325;
		int wsPort = 8005;
		boolean wsEnable = true;
		boolean wssEnable = false;
		String wssCert = null;
		String wssKey = null;

		try {
			Properties props = new Properties();
			File file = new File("server.properties");
			if (file.exists())
				props.load(new FileReader(file));
			else {
				Properties def = new Properties();
				def.load(getClass().getResourceAsStream("/server.properties"));
				def.store(new FileWriter(file), "Saved automatically");
			}

			port = Integer.parseInt(props.getProperty("server_port", "2325"));
			wsPort = Integer.parseInt(props.getProperty("websocket_port", "8005"));
			wsEnable = Boolean.parseBoolean(props.getProperty("websocket_enable", "true"));
			wssEnable = Boolean.parseBoolean(props.getProperty("wss_enable", "false"));
			wssCert = props.getProperty("wss_cert", null);
			wssKey = props.getProperty("wss_key", null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.port = port;
		this.wsPort = wsPort;
		this.wsEnable = wsEnable;

		if (wssEnable) {
			Validate.notNull(wssCert, "The certificate path cannot be null (wss_cert)");
			Validate.notNull(wssKey, "The private key path cannot be null (wss_key)");

			File cert = new File(wssCert);
			File key = new File(wssKey);

			Validate.isTrue(cert.exists(), "The certificate must exist : " + cert.getAbsolutePath());
			Validate.isTrue(key.exists(), "The private key must exist : " + cert.getAbsolutePath());

			try {
				sslContext = SslContextBuilder.forServer(cert, key).build();
			} catch (SSLException e) {
				e.printStackTrace();
				return;
			}
		} else {
			sslContext = null;
		}

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
						if (sslContext != null)
							pipeline.addLast(sslContext.newHandler(ch.alloc()));

						pipeline.addLast(new HttpServerCodec());
						pipeline.addLast(new HttpObjectAggregator(65536));
						// pipeline.addLast(new WebSocketServerCompressionHandler());
						pipeline.addLast(new WebSocketServerProtocolHandler("/game"));
						pipeline.addLast(new JsonCodec());
						pipeline.addLast(new JsonProtocolHandler());
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
		EventLoopGroup ws_bossGroup = null;
		EventLoopGroup ws_workerGroup = null;

		if (wsEnable) {
			ws_bossGroup = new NioEventLoopGroup();
			ws_workerGroup = new NioEventLoopGroup();
		}

		try {
			ChannelFuture main = setupMainServer(bossGroup, workerGroup);
			ChannelFuture ws = null;
			if (wsEnable)
				ws = setupWebSocket(ws_bossGroup, ws_workerGroup);

			main.channel().closeFuture().sync();
			if (wsEnable)
				ws.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();

			if (wsEnable) {
				ws_workerGroup.shutdownGracefully();
				ws_bossGroup.shutdownGracefully();
			}
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
