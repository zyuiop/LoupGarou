package net.zyuiop.loupgarou.network;

import java.util.concurrent.TimeUnit;
import android.app.Activity;
import android.content.Intent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.zyuiop.loupgarou.GameChooser;
import net.zyuiop.loupgarou.network.handlers.GameCompositionHandler;
import net.zyuiop.loupgarou.network.handlers.GameLeaveHandler;
import net.zyuiop.loupgarou.network.handlers.GameListHandler;
import net.zyuiop.loupgarou.network.handlers.JoinHandler;
import net.zyuiop.loupgarou.network.handlers.LoginHandler;
import net.zyuiop.loupgarou.network.handlers.MessageHandler;
import net.zyuiop.loupgarou.network.handlers.PhaseHandler;
import net.zyuiop.loupgarou.network.handlers.PlayersHandler;
import net.zyuiop.loupgarou.network.handlers.RoleHandler;
import net.zyuiop.loupgarou.network.handlers.StateHandler;
import net.zyuiop.loupgarou.network.handlers.VoteEndHandler;
import net.zyuiop.loupgarou.network.handlers.VoteHandler;
import net.zyuiop.loupgarou.network.handlers.VoteValueHandler;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.network.PacketDecoder;
import net.zyuiop.loupgarou.protocol.network.PacketEncoder;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameJoinConfirmPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameLeavePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameListPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.LoginResponsePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetGameCompositionPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetPhasePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetPlayersPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetRolePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetStatePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.VoteEndPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.VoteRequestPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.VoteValuePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.LoginPacket;
import net.zyuiop.loupgarou.protocol.threading.TaskManager;

/**
 * @author zyuiop
 */
public class NetworkManager {
	private static NetworkManager instance;

	private final String name;
	private final String ip;
	private final int port;
	private Channel channel;
	private boolean connected = false;
	private Activity currentStage = null;
	private EventLoopGroup workerGroup;
	private GameChooser gameChooser;

	public NetworkManager(String name, String ip, int port) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		instance = this;
	}

	public static NetworkManager getInstance() {
		return instance;
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
				ch.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new ProtocolHandler(NetworkManager.this));
			}
		});

		// Start the client
		channel = b.connect(ip, port).sync().channel();
		initHandlers();
		authentificate();
	}

	public void disconnect() {
		setConnected(false);
		stop();
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
		ProtocolHandler.handle(VoteValuePacket.class, new VoteValueHandler(this));
		ProtocolHandler.handle(SetGameCompositionPacket.class, new GameCompositionHandler(this));
	}

	private void authentificate() {
		send(new LoginPacket(name));
	}

	public void finishLogin() {
		setConnected(true);
		startActivity(GameChooser.class);
	}

	private void startActivity(final Class<? extends Activity> activity) {
		currentStage.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(currentStage, activity);
				currentStage.startActivity(intent);
			}
		});
	}

	public Activity getCurrentStage() {
		return currentStage;
	}

	public void setCurrentStage(Activity currentStage) {
		this.currentStage = currentStage;
	}

	protected void initMainWindow() {
		/*currentStage = homeWindow;
		currentStage.show();*/
	}

	public void send(final Packet packet) {
		if (channel != null) {
			TaskManager.runAsync(new Runnable() {
				@Override
				public void run() {
					channel.writeAndFlush(packet);
				}
			});
		}
	}

	public void stop() {
		/*if (homeWindow != null) {
			homeWindow.close();
			homeWindow = null;
		}

		if (gameWindow != null) {
			gameWindow.close();
			gameWindow = null;
		}*/

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
		/*if (!(currentStage instanceof GameWindow)) {
			if (currentStage != null)
				currentStage.close();
			currentStage = gameWindow;
			gameWindow.acceptGame(packet);
			currentStage.show();
		}*/
	}

	public void leaveGame() {
		/*if (currentStage != null && currentStage == getHomeWindow())
			return;
		if (currentStage != null) {
			currentStage.close();
		}
		initMainWindow();*/
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		/*if (connected) {
			if (!Platform.isFxApplicationThread()) {
				Platform.runLater(LoginWindow.getInstance()::close);
			} else {
				LoginWindow.getInstance().close();
			}
		}*/
		this.connected = connected;
	}

	public void setGameChooser(GameChooser gameChooser) {
		this.gameChooser = gameChooser;
	}
}
