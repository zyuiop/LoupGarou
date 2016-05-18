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
import net.zyuiop.loupgarou.client.auth.AuthenticationService;
import net.zyuiop.loupgarou.client.LGClient;
import net.zyuiop.loupgarou.client.gui.GameWindow;
import net.zyuiop.loupgarou.client.gui.HomeWindow;
import net.zyuiop.loupgarou.client.gui.LoginStatusWindow;
import net.zyuiop.loupgarou.client.net.handlers.*;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.network.PacketDecoder;
import net.zyuiop.loupgarou.protocol.network.PacketEncoder;
import net.zyuiop.loupgarou.protocol.packets.clientbound.*;
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
	private GameWindow gameWindow;

	public NetworkManager(String name, String ip, int port) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.homeWindow = new HomeWindow(this);
		this.gameWindow = new GameWindow(this);
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
		ProtocolHandler.handle(MessagePacket.class, new MessageHandler(this));
		ProtocolHandler.handle(GameJoinConfirmPacket.class, new JoinHandler(this));
		ProtocolHandler.handle(SetPlayersPacket.class, new PlayersHandler(this));
		ProtocolHandler.handle(SetRolePacket.class, new RoleHandler(this));
		ProtocolHandler.handle(SetPhasePacket.class, new PhaseHandler(this));
		ProtocolHandler.handle(SetStatePacket.class, new StateHandler(this));
		ProtocolHandler.handle(GameLeavePacket.class, new GameLeaveHandler(this));
		ProtocolHandler.handle(VoteRequestPacket.class, new VoteHandler(this));
		ProtocolHandler.handle(VoteEndPacket.class, new VoteEndHandler(this));
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

	public GameWindow getGameWindow() {
		return gameWindow;
	}

	public void joinGame(GameJoinConfirmPacket packet) {
		// Todo : handle other game
		if (!(currentStage instanceof GameWindow)) {
			if (currentStage != null)
				currentStage.close();
			currentStage = gameWindow;
			gameWindow.acceptGame(packet);
			currentStage.show();
		}
	}

	public void leaveGame() {
		if (currentStage != null && currentStage == getHomeWindow())
			return;
		if (currentStage != null) {
			currentStage.close();
		}
		initMainWindow();
	}
}
