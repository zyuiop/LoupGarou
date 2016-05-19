package net.zyuiop.loupgarou.server.game;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.network.GameInfo;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameLeavePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameListPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.LoginResponsePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.CreateGamePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.JoinGamePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.RefreshGameListPacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.SendMessagePacket;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.network.ConnectedClient;
import net.zyuiop.loupgarou.server.network.ProtocolHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author zyuiop
 */
public class GamesManager {
	private static       Pattern            namePattern = Pattern.compile("^[a-zA-Z0-9 _-]{5,25}$");
	private static       int                nextGameId  = 1;
	private static final Map<Integer, Game> games       = new HashMap<>();

	public static void init() {
		ProtocolHandler.handle(JoinGamePacket.class, (packet, client) -> {
			int id = packet.getGameId();
			if (id == -1) {
				leaveGame(client.getPlayer());
				client.sendPacket(new GameListPacket(getInfos()));
				return;
			}

			Game game = getGame(id);
			if (game == null) {
				client.sendPacket(new MessagePacket(MessageType.SYSTEM, "Cette partie n'existe pas !"));
			} else {
				game.handleJoin(client.getPlayer());
			}
		});

		ProtocolHandler.handle(CreateGamePacket.class, ((packet, client) -> {
			if (!namePattern.matcher(packet.getName()).find()) {
				client.sendPacket(new MessagePacket(MessageType.ERROR, "Nom de partie invalide : \nde 5 à 25 caractères alphanumériques."));
				return;
			}

			if (!Arrays.asList(packet.getCharacters()).contains(Role.WOLF)) {
				client.sendPacket(new MessagePacket(MessageType.ERROR, "Partie invalide :\nIl est nécessaire d'avoir au moins un loup."));
				return;
			}

			GameConfig config = new GameConfig(packet.getName(), client.getName(), packet.getPlayers(), packet.getCharacters());


			Game game = createGame(config);
			game.handleJoin(client.getPlayer());
		}));

		ProtocolHandler.handle(SendMessagePacket.class, (packet, client) -> {
			GamePlayer player = client.getPlayer();
			if (player.getGame() != null) {
				player.getGame().sendMessage(player, packet.getMessage());
			}
		});

		ProtocolHandler.handle(RefreshGameListPacket.class, (((packet, client) -> client.sendPacket(new GameListPacket(getInfos())))));
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
			player.getGame().confirmJoin(player);
		} else {
			player.sendPacket(new GameListPacket(getInfos()));
		}
	}

	public static void leaveGame(GamePlayer player) {
		Game game = player.getGame();
		if (game != null) {
			game.removePlayer(player);
			player.sendPacket(new GameLeavePacket("Partie quittée"));
			player.setGame(null);
		} else {
			player.sendPacket(new GameLeavePacket("Partie quittée"));
		}
	}

	public static void removeGame(Game game) {
		// TODO meilleure sortie de jeu (envoi de packet)
		game.getEveryone().stream().filter(player -> player.getGame() == game).forEach(player -> {
			player.setGame(null);
			player.sendPacket(new GameLeavePacket("Partie terminée"));
		});

		games.remove(game.getGameId());
	}
}
