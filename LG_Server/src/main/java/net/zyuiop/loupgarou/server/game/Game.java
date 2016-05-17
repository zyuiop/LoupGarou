package net.zyuiop.loupgarou.server.game;

import com.google.common.collect.Lists;
import net.zyuiop.loupgarou.game.GamePhase;
import net.zyuiop.loupgarou.game.GameState;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.network.GameInfo;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameStatePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetPhasePacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.SetStatePacket;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class Game {
	private final int              gameId;
	private       GameState        state      = GameState.WAITING;
	private       GamePhase        phase      = null;
	private       List<GamePlayer> players    = new ArrayList<>();
	private       List<GamePlayer> spectators = new ArrayList<>();
	private final GameConfig config;

	protected Game(int gameId, GameConfig config) {
		this.gameId = gameId;
		this.config = config;
	}

	protected void handleJoin(GamePlayer player) {
		if (state == GameState.WAITING) {
			if (!players.contains(player)) {
				if (players.size() >= config.getPlayers()) {
					spectators.add(player);
					player.getClient().sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous rejoignez en tant que spectateur !"));
					return;
				}

				spectators.remove(player);
				players.add(player);
				player.setGame(this);
				player.getClient().sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous avez rejoint la partie !"));
				sendToAll(new MessagePacket(MessageType.SYSTEM, player.getName() + " a rejoint la partie !"));
				sendGameState(player);

				if (players.size() >= config.getPlayers())
					start();
			} else {
				player.getClient().sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous êtes déjà dans cette partie !"));
				sendGameState(player);
			}
		} else {
			if (!players.contains(player)) {
				spectators.add(player);
				player.getClient().sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous rejoignez en tant que spectateur !"));
				sendGameState(player);
			}
		}
	}

	protected void sendGameState(GamePlayer player) {
		player.getClient().sendPacket(new GameStatePacket(gameId, config.getName(), config.getHoster(), config.getCharacters(), getPlayerList()));
	}

	private void sendToPlayers(Packet packet) {
		players.forEach(pl -> pl.getClient().sendPacket(packet));
	}

	private void sendToSpectators(Packet packet) {
		spectators.forEach(pl -> pl.getClient().sendPacket(packet));
	}

	private void sendToAll(Packet packet) {
		sendToPlayers(packet);
		sendToSpectators(packet);
	}

	public String[] getPlayerList() {
		List<String> lst = players.stream().map(GamePlayer::getName).collect(Collectors.toList());
		return lst.toArray(new String[lst.size()]);
	}

	private void sendToAll(Packet packet, Role role, Role... roles) {
		getPlayers(role, roles).forEach(pl -> pl.getClient().sendPacket(packet));
	}

	private Collection<GamePlayer> getPlayers(Role role, Role... roles) {
		List<Role> allRoles = Lists.newArrayList(roles);
		allRoles.add(role);
		return players.stream().filter(pl -> allRoles.contains(pl.getRole())).collect(Collectors.toList());
	}

	private Collection<GamePlayer> getPlayersExcepted(Role role, Role... roles) {
		List<Role> allRoles = Lists.newArrayList(roles);
		allRoles.add(role);
		return players.stream().filter(player -> !allRoles.contains(player.getRole())).collect(Collectors.toList());
	}

	public GameInfo getGameInfo() {
		return new GameInfo(gameId, config.getName(), config.getHoster(), state, players.size(), config.getPlayers());
	}

	public void start() {
		setState(GameState.PREPARING);
		sendToAll(new SetStatePacket(state));

		// Shuffle roles
		List<Role> availableRoles = Lists.newArrayList(config.getCharacters());
		for (int i = 0; i < config.getVillagers(); i++) {
			availableRoles.add(Role.VILLAGER);
		}

		Collections.shuffle(availableRoles);
		Iterator<Role> iterator = availableRoles.iterator();
		Iterator<GamePlayer> playersIterator = players.iterator();
		while (iterator.hasNext()) {
			if (playersIterator.hasNext()) {
				playersIterator.next().setRole(iterator.next());
				iterator.remove();
			} else {
				break;
			}
		}

		// Start game
		setState(GameState.STARTED);
		setPhase(GamePhase.PREPARATION);

		startThief(availableRoles);
	}

	private void startThief(List<Role> remaining) {
		Collection<GamePlayer> stealers = getPlayers(Role.THIEF);
		if (stealers.size() > 0) {
			String[] available = new String[2];
			if (remaining.size() >= 2) {
				available[0] = remaining.get(0).name();
				available[1] = remaining.get(1).name();
			} else if (remaining.size() == 1) {
				available[0] = remaining.get(0).name();
				available[1] = Role.VILLAGER.name();
			} else {
				available[0] = available[1] = Role.VILLAGER.name();
			}

			GamePlayer thief = stealers.iterator().next();
			sendToAll(new MessagePacket(MessageType.GAME, "Le voleur doit désormais choisir son nouveau rôle..."));
			new Vote(30, this, "Choisissez votre nouveau rôle", stealers, available) {
				@Override
				protected void handleResults(Map<GamePlayer, String> results) {
					Map.Entry<GamePlayer, String> entry = results.entrySet().iterator().next();
					if (entry == null) {
						thief.sendPacket(new MessagePacket(MessageType.GAME, "Vous n'avez choisi aucune autre classe, vous restez donc voleur."));
					} else {
						Role role = Role.valueOf(entry.getValue());
						thief.setRole(role);
					}
					startPrepareTurn();
				}
			};
		} else {
			startPrepareTurn();
		}
	}

	private void startPrepareTurn() {
		Collection<GamePlayer> cupidon = getPlayers(Role.CUPIDON);
		if (cupidon.size() > 0) {
			Collection<String> others = players.stream().filter(player -> player.getRole() != Role.CUPIDON).map(GamePlayer::getName).collect(Collectors.toList());
			String[] choices = others.toArray(new String[others.size()]);

			sendToAll(new MessagePacket(MessageType.GAME, "Cupdion désigne les deux amoureux..."));

			CupidonVote first = new CupidonVote(60, this, "Premier amoureux", cupidon, choices);
			CupidonVote second = new CupidonVote(60, this, "Second amoureux", cupidon, choices);

			first.setOtherVote(second);
			first.setRunAtEnd(this::finishPreparation);
			second.setOtherVote(first);
			second.setRunAtEnd(this::finishPreparation);
		} else {
			finishPreparation();
		}
	}

	private void finishPreparation() {
		runNight();
	}

	private boolean checkWin() {
		if (getPlayers(Role.WOLF).size() == 0) {
			sendToAll(new MessagePacket(MessageType.GAME, "Victoire du village !"));
			return true;
		} else if (getPlayersExcepted(Role.WOLF).size() == 0) {
			sendToAll(new MessagePacket(MessageType.GAME, "Victoire des Loups !"));
			return true;
		}
		return false;
	}

	private boolean hasToEnd() {
		if (!checkWin())
			return false;
		setState(GameState.FINISHED);
		return true;
	}

	private void runNight() {
		if (hasToEnd())
			return;

		setPhase(GamePhase.NIGHT);

		new Vote(60, this, "Choisissez votre prochaine victime", getPlayers(Role.WOLF), getPlayersExcepted(Role.WOLF).stream().map(GamePlayer::getName).collect(Collectors.toList())) {
			@Override
			protected void handleResults(Map<GamePlayer, String> results) {
				Map<String, Integer> voted = new HashMap<>();
				for (String val : results.values()) {
					if (!voted.containsKey(val))
						voted.put(val, 0);
					voted.put(val, voted.get(val) + 1);
				}

				int max = 0;
				String val = null;
				for (Map.Entry<String, Integer> vote : voted.entrySet()) {
					if (vote.getValue() > max) {
						max = vote.getValue();
						val = vote.getKey();
					}
				}

				// TODO : Handle witch
				GamePlayer player = GamePlayer.getPlayer(val);
				players.remove(player);
				spectators.add(player);
				player.sendMessage(MessageType.GAME, "Vous avez été croqué par les loups !");
				sendToAll(new MessagePacket(MessageType.GAME, "Vous avez croqué " + player.getName() + " !"), Role.WOLF);

				// TODO : other roles
				runDay();
			}
		};
	}

	private void runPreNight() {

	}

	private void runEndNight() {

	}

	private void runDay() {
		if (hasToEnd())
			return;

		setPhase(GamePhase.DAY);

		// TODO : custom time
		new Vote(60, this, "Vote du village", players, players.stream().map(GamePlayer::getName).collect(Collectors.toList())) {
			@Override
			protected void handleResults(Map<GamePlayer, String> results) {
				Map<String, Integer> voted = new HashMap<>();
				for (String val : results.values()) {
					if (!voted.containsKey(val))
						voted.put(val, 0);
					voted.put(val, voted.get(val) + 1);
				}

				int max = 0;
				String val = null;
				for (Map.Entry<String, Integer> vote : voted.entrySet()) {
					if (vote.getValue() > max) {
						max = vote.getValue();
						val = vote.getKey();
					}
				}

				// TODO : Handle equality
				GamePlayer player = GamePlayer.getPlayer(val);
				players.remove(player);
				spectators.add(player);
				player.sendMessage(MessageType.GAME, "Vous avez été tué par le village !");
				sendToAll(new MessagePacket(MessageType.GAME, "Vous avez éliminé " + player.getName() + " ! Il était " + player.getRole()));

				// TODO : prenight
				runNight();
			}
		};
	}

	private void setState(GameState state) {
		this.state = state;
		sendToAll(new SetStatePacket(state));
	}

	private void setPhase(GamePhase phase) {
		this.phase = phase;
		sendToAll(new SetPhasePacket(phase));
	}
}
