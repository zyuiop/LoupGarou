package net.zyuiop.loupgarou.server.game;

import net.zyuiop.loupgarou.game.GameState;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.network.GameInfo;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameLeavePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameListPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.*;
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
				leaveGame(client.getPlayer(), true);
				client.sendPacket(new GameListPacket(getInfos()));
				return;
			}

			Game game = getGame(id);
			if (game == null) {
				client.sendPacket(new MessagePacket(MessageType.ERROR, "Cette partie n'existe pas !"));
			} else {
				if (!game.checkPassword(packet.getPassword())) {
					client.sendPacket(new MessagePacket(MessageType.ERROR, "Mot de passe incorrect !"));
					return;
				}

				game.handleJoin(client.getPlayer());
			}
		});

		ProtocolHandler.handle(CreateGamePacket.class, ((packet, client) -> {
			if (!Arrays.asList(packet.getCharacters()).contains(Role.WOLF)) {
				client.sendPacket(new MessagePacket(MessageType.ERROR, "Partie invalide :\nIl est nécessaire d'avoir au moins un loup."));
				return;
			}

			if (!namePattern.matcher(packet.getName()).find()) {
				client.sendPacket(new MessagePacket(MessageType.ERROR, "Nom de partie invalide : \nde 5 à 25 caractères alphanumériques."));
				return;
			}
			GameConfig config = new GameConfig(packet.getName(), client.getName(), packet.getPlayers(), packet.getPassword(), packet.getCharacters());


			Game game = createGame(config);
			game.handleJoin(client.getPlayer());
		}));

		ProtocolHandler.handle(ChangeGameCompositionPacket.class, ((packet, client) -> {
			if (!Arrays.asList(packet.getCharacters()).contains(Role.WOLF)) {
				client.sendPacket(new MessagePacket(MessageType.ERROR, "Partie invalide :\nIl est nécessaire d'avoir au moins un loup."));
				return;
			}

			if (client.getPlayer().getGame() != null) {
				Game game = client.getPlayer().getGame();
				if (game.getGameInfo().getHoster().equalsIgnoreCase(client.getPlayer().getName())) {
					// Edit game
					if (game.getState() == GameState.WAITING) {
						if (packet.getPlayers() < game.getPlayers().size()) {
							client.getPlayer().sendMessage(MessageType.ERROR, "Impossible de modifier la partie :\nIl y a plus de joueurs connectés que de places !");
							return;
						}

						GameInfo info = game.getGameInfo();
						GameConfig config = new GameConfig(info.getGameName(), info.getHoster(), packet.getPlayers(), game.getConfig().getPassword(), packet.getCharacters());
						game.setConfig(config);
						client.getPlayer().sendMessage(MessageType.SYSTEM, "Vous avez bien modifié la partie.");
					} else {
						client.getPlayer().sendMessage(MessageType.ERROR, "Impossible de modifier la partie :\nImpossible de modifier une partie démarrée !");
					}
				} else {
					client.getPlayer().sendMessage(MessageType.ERROR, "Impossible de modifier la partie :\nVous n'êtes pas l'host de cette partie.");
				}
			} else {
				client.getPlayer().sendMessage(MessageType.ERROR, "Impossible de modifier la partie :\nVous n'êtes pas dans une partie.");
			}
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
			player.getGame().handleJoin(player);
		} else {
			player.sendPacket(new GameListPacket(getInfos()));
		}
	}

	public static void leaveGame(GamePlayer player, boolean voluntary) {
		Game game = player.getGame();
		if (game != null) {
			if (voluntary) {
				game.leaveRoom(player);
				player.sendPacket(new GameLeavePacket("Partie quittée"));
				player.setGame(null);
			} else {
				game.removePlayer(player);
			}
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

	public static void disconnect(GamePlayer player) {
		Game game = player.getGame();
		if (game != null) {
			if (game.getState() != GameState.STARTED) {
				game.removePlayer(player);
			}
		}
	}
}
