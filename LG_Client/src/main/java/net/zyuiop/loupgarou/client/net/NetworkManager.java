package net.zyuiop.loupgarou.client.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.zyuiop.loupgarou.client.AuthenticationService;
import net.zyuiop.loupgarou.client.LGClient;
import net.zyuiop.loupgarou.client.ProtocolHandler;
import net.zyuiop.loupgarou.client.gui.HomeWindow;
import net.zyuiop.loupgarou.client.gui.LoginStatusWindow;
import net.zyuiop.loupgarou.client.net.handlers.LoginHandler;
import net.zyuiop.loupgarou.client.net.handlers.GameListHandler;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.network.PacketDecoder;
import net.zyuiop.loupgarou.protocol.network.PacketEncoder;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameListPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.LoginResponsePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.LoginPacket;

import static net.zyuiop.loupgarou.client.LGClient.logger;

/**
 * @author zyuiop
 */
public class NetworkManager {
	private final String  name;
	private final String  ip;
	private final int     port;
	private       Channel channel;
	private Stage currentStage = null;
	private HomeWindow homeWindow;

	public NetworkManager(String name, String ip, int port) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.homeWindow = new HomeWindow(this);
	}

	public void connect() throws Exception {
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap(); // (1)
		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new ProtocolHandler());
			}
		});

		currentStage = new LoginStatusWindow();
		currentStage.show();

		// Start the client
		channel = b.connect(ip, port).sync().channel();
		logger.info("Connected to " + ip + ". Trying to login...");
		initHandlers();
		authentificate();
	}

	private void initHandlers() {
		ProtocolHandler.handle(LoginResponsePacket.class, new LoginHandler(this));
		ProtocolHandler.handle(GameListPacket.class, new GameListHandler(this));
	}

	private void authentificate() {
		long time = System.currentTimeMillis();
		AuthenticationService service = LGClient.authService;

		send(new LoginPacket(name, time, service.getPublicKey(name), service.signData(time, name)));
	}

	public void finishLogin() {
		logger.info("Logged in !");
		Platform.runLater(() -> {
			currentStage.close();
			initMainWindow();
		});
	}

	protected void initMainWindow() {
		currentStage = homeWindow;
		currentStage.show();
	}

	public void send(Packet packet) {
		if (channel != null) {
			channel.writeAndFlush(packet);
		}
	}

	public void stop() {

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

	public void closeWindow() {
		if (Platform.isFxApplicationThread())
			currentStage.close();
		else
			Platform.runLater(currentStage::close);
	}

	public Stage getCurrentStage() {
		return currentStage;
	}

	public HomeWindow getHomeWindow() {
		return homeWindow;
	}
}
