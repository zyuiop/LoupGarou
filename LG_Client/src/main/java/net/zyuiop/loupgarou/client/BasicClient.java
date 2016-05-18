package net.zyuiop.loupgarou.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.network.GameInfo;
import net.zyuiop.loupgarou.protocol.network.PacketDecoder;
import net.zyuiop.loupgarou.protocol.network.PacketEncoder;
import net.zyuiop.loupgarou.protocol.packets.clientbound.*;
import net.zyuiop.loupgarou.protocol.packets.serverbound.CreateGamePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.JoinGamePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.LoginPacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.VotePacket;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author zyuiop
 */
public class BasicClient {
	public static final Logger logger = LogManager.getRootLogger();

	public static void main(String[] args) throws NoSuchAlgorithmException {
		int port = 2325;
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		logger.info("Loading LoupGarou CLI-Client...");
		AuthenticationService service = new RSAAuthenticationService(new File(System.getProperty("user.dir")));
		if (!service.hasKey()) {
			logger.info("No RSA key found, generating it...");
			service.generateKeyPair();
		}

		String ip;
		String name;

		Scanner scanner = new Scanner(System.in);

		if (args.length > 0)
			ip = args[0];
		else {
			System.out.print("IP > ");
			ip = scanner.next();
		}

		if (args.length > 1)
			name = args[1];
		else {
			System.out.print("Name > ");
			name = scanner.next();
		}

		try {
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

			// Start the client

			ChannelFuture f = b.connect(ip, port).sync();
			logger.info("Connected to " + ip + ".");
			long time = System.currentTimeMillis();

			// Handlers

			ProtocolHandler.handle(GameListPacket.class, packet -> {
				logger.info("Received games (type 'join <id>' to join one) : ");
				for (GameInfo info : packet.getGames()) {
					logger.info(" - " + info.getId() + " : " + info.getGameName() + ", hosted by " + info.getHoster() + ", " + info.getState() + ", " + info.getCurrentPlayers() + "/" + info.getMaxPlayers());
				}
			});

			ProtocolHandler.handle(GameStatePacket.class, packet -> {
				logger.info("Game joined : " + packet.getName() + ", hosted by " + packet.getHoster());
			});


			ProtocolHandler.handle(SetPhasePacket.class, packet -> {
				logger.info("Game phase : " + packet.getPhase());
			});

			ProtocolHandler.handle(MessagePacket.class, packet -> {
				switch (packet.getType()) {
					case SYSTEM:
						logger.info("[Message] (System) " + packet.getMessage());
						break;
					case GAME:
						logger.info("[Message] (Game) " + packet.getMessage());
						break;
					case USER:
						logger.info("[Message] (User : " + packet.getSender() + ") " + packet.getMessage());
						break;
				}
			});

			ProtocolHandler.handle(SetStatePacket.class, packet -> logger.info("GameState changed to : " + packet.getState()));

			ProtocolHandler.handle(VoteRequestPacket.class, packet -> {
				logger.info("Vote #" + packet.getVoteId() + " started (" + packet.getChooseReason() + ") ! Use 'vote <id> <value>' to vote.");
				for (String val : packet.getAvailableChoices())
					logger.info(" - " + val);
			});

			ProtocolHandler.handle(VoteEndPacket.class, packet -> logger.info("Vote #" + packet.getVoteId() + " has terminated."));

			f.channel().writeAndFlush(new LoginPacket(name, time, service.getPublicKey(), service.signData(time, name))).sync();

			while (f.channel().isActive()) {
				String command = scanner.nextLine();
				String[] parts = command.split(" ");
				if (parts[0].equalsIgnoreCase("join") && parts.length > 1) {
					Integer id = Integer.parseInt(parts[1]);
					logger.info("Attempting to join game " + id + "...");
					f.channel().writeAndFlush(new JoinGamePacket(id)).sync();
				} else if (parts[0].equalsIgnoreCase("quit")) {
					f.channel().close().sync();
				} else if (parts[0].equalsIgnoreCase("create") && parts.length > 3) {
					String chanName = parts[1];
					int players = Integer.parseInt(parts[2]);
					List<Role> roles = new ArrayList<>();
					for (int i = 3; i < parts.length; i++)
						roles.add(Role.valueOf(parts[i]));

					f.channel().writeAndFlush(new CreateGamePacket(chanName, players, roles.toArray(new Role[roles.size()]))).sync();
				} else if (parts[0].equalsIgnoreCase("vote") && parts.length > 2) {
					int voteId = Integer.parseInt(parts[1]);
					String vote = StringUtils.join(Arrays.copyOfRange(parts, 2, parts.length), " ");

					f.channel().writeAndFlush(new VotePacket(voteId, vote)).sync();
				} else {
					logger.warn("Unknown command");
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}
}
