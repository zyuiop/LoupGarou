package net.zyuiop.loupgarou.websocket.net;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.SystemPropertyUtil;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.network.PacketDecoder;
import net.zyuiop.loupgarou.protocol.network.PacketEncoder;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameJoinConfirmPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.LoginPacket;
import net.zyuiop.loupgarou.protocol.threading.TaskManager;
import net.zyuiop.loupgarou.websocket.SocketServer;
import net.zyuiop.loupgarou.websocket.webprotocol.GsonManager;

/**
 * @author zyuiop
 */
public class NetworkManager {
	private final String name;
	private final String ip;
	private final int port;
	private final Session session;
	private final Logger logger;
	private final ProtocolHandler handler = new ProtocolHandler(this);
	private Channel channel;
	private EventLoopGroup workerGroup;

	private long lastPing = -1;
	private long lastPong = -1;
	private ScheduledFuture pingTask;

	public NetworkManager(String name, String ip, int port, Session session) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.session = session;

		logger = Logger.getLogger(name + "/" + session.getId());

		pingTask = SocketServer.getScheduler().scheduleAtFixedRate(() -> {
			// Example :
			// lastPing = 4000
			// lastPong = 1000
			// lastPing - lastPong = 3000

			if (lastPing - lastPong >= 30000) {
				// 30 seconds without anything
				try {
					session.getBasicRemote().sendText(GsonManager.getGson().toJson(new MessagePacket(MessageType.ERROR, "Timed Out"), Packet.class));
					session.close(new CloseReason(() -> 4001, "Connexion timed out"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				this.stop();
			} else {
				try {
					session.getBasicRemote().sendText("PING");
					lastPing = System.currentTimeMillis();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 10, 10, TimeUnit.SECONDS);
	}

	public void receivePong() {
		lastPong = System.currentTimeMillis();
	}

	public void connect() throws Exception {
		workerGroup = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap(); // (1)
		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), handler);
			}
		});

		// Start the client
		try {
			channel = b.connect(ip, port).sync().channel();
			logger.info("Connected to " + ip + ". Trying to login...");
			// LoginWindow.getInstance().setStatus("Authentification auprès de " + ip + ":" + port + "...");
			authentificate();
		} catch (Exception e) {
			SocketServer.logger.info("Closing " + getSession().getId() + " : connexion failed.");
			if (getSession().isOpen())
				getSession().close(new CloseReason(() -> 4000, e.getClass().getName() + " " + e.getMessage()));
		}
	}

	private void authentificate() {
		send(new LoginPacket(name));
	}

	public void send(Packet packet) {
		if (channel != null) {
			TaskManager.runAsync(() -> channel.writeAndFlush(packet));
		}
	}

	public void stop() {
		if (channel != null && channel.isOpen()) {
			channel.close();
			channel = null;
		}

		if (workerGroup != null) {
			try {
				workerGroup.shutdownGracefully(500, 3000, TimeUnit.MILLISECONDS).sync();
				workerGroup = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		pingTask.cancel(true);

		logger.info("Disconnected from " + ip);
	}

	public String getName() {
		return name;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public Session getSession() {
		return session;
	}

	public Logger getLogger() {
		return logger;
	}
}
