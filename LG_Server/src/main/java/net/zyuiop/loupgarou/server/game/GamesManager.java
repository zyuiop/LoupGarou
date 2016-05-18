package net.zyuiop.loupgarou.server.game;

import net.zyuiop.loupgarou.protocol.network.GameInfo;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameListPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.CreateGamePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.JoinGamePacket;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.network.ConnectedClient;
import net.zyuiop.loupgarou.server.network.ProtocolHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class GamesManager {
	private static       int                nextGameId = 1;
	private static final Map<Integer, Game> games      = new HashMap<>();

	public static void init() {
		ProtocolHandler.handle(JoinGamePacket.class, (packet, client) -> {
			Game game = getGame(packet.getGameId());
			if (game == null) {
				client.sendPacket(new MessagePacket(MessageType.SYSTEM, "Cette partie n'existe pas !"));
			} else {
				game.handleJoin(client.getPlayer());
			}
		});

		ProtocolHandler.handle(CreateGamePacket.class, ((packet, client) -> {
			GameConfig config = new GameConfig(packet.getName(), client.getName(), packet.getPlayers(), packet.getCharacters());
			Game game = createGame(config);
			game.handleJoin(client.getPlayer());
		}));
	}

	public static Game getGame(int id) {
		return games.get(id);
	}

	public static GameInfo[] getInfos() {
		GameInfo[] infos = new GameInfo[games.size()];
		int i = 0;
		for (Game game : games.values()) {
			infos[i++] = game.getGameInfo();
		}
		return infos;
	}

	public static Game createGame(GameConfig config) {
		int id = nextGameId++;
		Game game = new Game(id, config);
		games.put(id, game);
		LGServer.getLogger().info("Creating game with id " + id + " : " + config.toString());
		return game;
	}

	public static void login(ConnectedClient client) {
		GamePlayer player = client.getPlayer();
		if (player.getGame() != null) {
			player.sendMessage(MessageType.SYSTEM, "You joined your game automatically !");
			player.getGame().sendGameState(player);
		} else {
			player.sendPacket(new GameListPacket(getInfos()));
		}
	}

	public static void removeGame(Game game) {
		// TODO meilleure sortie de jeu (envoi de packet)
		game.getEveryone().stream().filter(player -> player.getGame() == game).forEach(player -> {
			player.setGame(null);
		});

		games.remove(game.getGameId());
	}
}
