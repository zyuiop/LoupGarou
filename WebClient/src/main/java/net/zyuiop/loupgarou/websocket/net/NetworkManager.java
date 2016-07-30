package net.zyuiop.loupgarou.websocket.net;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
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
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.network.PacketDecoder;
import net.zyuiop.loupgarou.protocol.network.PacketEncoder;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameJoinConfirmPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.LoginPacket;
import net.zyuiop.loupgarou.protocol.threading.TaskManager;
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
	private boolean connected = false;
	private EventLoopGroup workerGroup;

	public NetworkManager(String name, String ip, int port, Session session) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.session = session;

		logger = Logger.getLogger(name + "/" + session.getId());
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
		channel = b.connect(ip, port).addListener(future -> {
			if (!future.isSuccess()) {
				String converted = GsonManager.getGson().toJson(new MessagePacket(MessageType.ERROR, "Impossible de se connecter au serveur : " + future.cause().getMessage()), Packet.class);
				getSession().getBasicRemote().sendText(converted);
			}
		}).sync().channel();
		logger.info("Connected to " + ip + ". Trying to login...");
		// LoginWindow.getInstance().setStatus("Authentification auprès de " + ip + ":" + port + "...");
		authentificate();
	}

	private void authentificate() {
		send(new LoginPacket(name));
	}

	public void finishLogin() {
		logger.info("Logged in !");
		setConnected(true);
		// Platform.runLater(() -> {
		// 	if (currentStage != null)
		// 		currentStage.close();
		// 	initMainWindow();
		// });
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

	public void joinGame(GameJoinConfirmPacket packet) {
		// Todo : handle other game
		// if (!(currentStage instanceof GameWindow)) {
		// 	if (currentStage != null)
		// 		currentStage.close();
		// 	currentStage = gameWindow;
		// 	gameWindow.acceptGame(packet);
		// 	currentStage.show();
		// }
	}

	public void leaveGame() {
		// if (currentStage != null && currentStage == getHomeWindow())
		// 	return;
		// if (currentStage != null) {
		// 	currentStage.close();
		// }
		// initMainWindow();
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public Session getSession() {
		return session;
	}

	public Logger getLogger() {
		return logger;
	}
}
