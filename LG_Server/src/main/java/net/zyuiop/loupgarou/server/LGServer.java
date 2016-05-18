package net.zyuiop.loupgarou.server;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.zyuiop.loupgarou.protocol.network.PacketDecoder;
import net.zyuiop.loupgarou.protocol.network.PacketEncoder;
import net.zyuiop.loupgarou.server.auth.AuthenticationService;
import net.zyuiop.loupgarou.server.auth.RSAAuthenticationService;
import net.zyuiop.loupgarou.server.game.GamesManager;
import net.zyuiop.loupgarou.server.network.ProtocolHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author zyuiop
 */
public class LGServer {
	private static LGServer instance;
	private static final Logger logger = LogManager.getRootLogger();
	private final int port;
	private AuthenticationService authenticationService;


	LGServer() throws NoSuchAlgorithmException {
		instance = this;
		this.port = 2325;

		// TODO read conf
		logger.info("Starting LGServer on port " + port + "...");
		authenticationService = new RSAAuthenticationService(new File(System.getProperty("user.dir") + "/users/"));
		GamesManager.init();

		try {
			run();
		} catch (Exception e) {
			logger.catching(Level.ERROR, e);
		}
	}

	private void run() throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap(); // (2)
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class) // (3)
					.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new ProtocolHandler());
						}
					})
					.option(ChannelOption.SO_BACKLOG, 128)          // (5)
					.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

			// Bind and start to accept incoming connections.
			ChannelFuture f = b.bind(port).sync(); // (7)

			// Wait until the server socket is closed.
			// In this example, this does not happen, but you can do that to gracefully
			// shut down your server.
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
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
