package net.zyuiop.loupgarou.server.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.zyuiop.loupgarou.game.GamePhase;
import net.zyuiop.loupgarou.game.GameState;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.game.tasks.DelayedTask;
import net.zyuiop.loupgarou.game.tasks.Task;
import net.zyuiop.loupgarou.game.tasks.TaskManager;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.network.GameInfo;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.*;
import net.zyuiop.loupgarou.server.game.phases.Phases;
import net.zyuiop.loupgarou.server.game.phases.PreparationPhase;
import net.zyuiop.loupgarou.server.game.tasks.HunterTask;
import net.zyuiop.loupgarou.server.game.votes.MajorityVote;
import net.zyuiop.loupgarou.server.utils.TaskChainer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class Game {
	private final int gameId;
	private GameState        state      = GameState.WAITING;
	private GamePhase        phase      = null;
	private List<GamePlayer> players    = new ArrayList<>();
	private List<GamePlayer> spectators = new ArrayList<>();
	private final GameConfig config;

	// Running variables
	private GamePlayer      nextVictim      = null;
	private Set<GamePlayer> otherVictims    = Sets.newHashSet();
	private GamePlayer      mayor           = null;
	private String          protectedPlayer = null;

	protected Game(int gameId, GameConfig config) {
		this.gameId = gameId;
		this.config = config;
	}

	protected void handleJoin(GamePlayer player) {
		if (state == GameState.WAITING) {
			if (!players.contains(player)) {
				if (players.size() >= config.getPlayers()) {
					spectators.add(player);
					player.sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous rejoignez en tant que spectateur !"));
					return;
				}

				spectators.remove(player);
				players.add(player);
				broadcastPlayerChange();
				player.setGame(this);
				player.sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous avez rejoint la partie !"));
				sendToAll(new MessagePacket(MessageType.SYSTEM, player.getName() + " a rejoint la partie !"));
				confirmJoin(player);

				if (players.size() >= config.getPlayers())
					start();
			} else {
				player.sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous êtes déjà dans cette partie !"));
				confirmJoin(player);
			}
		} else {
			if (!players.contains(player)) {
				spectators.add(player);
				player.getClient().sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous rejoignez en tant que spectateur !"));
				confirmJoin(player);
			}
		}
	}

	protected void confirmJoin(GamePlayer player) {
		player.sendPacket(new GameJoinConfirmPacket(gameId, config.getName(), config.getHoster()));
		sendGameState(player);
	}

	protected void sendGameState(GamePlayer player) {
		player.sendPacket(new SetPhasePacket(phase));
		player.sendPacket(new SetStatePacket(state));
		player.sendPacket(new SetPlayersPacket(config.getPlayers(), getPlayerList()));
		if (state == GameState.STARTED)
			player.sendPacket(new SetRolePacket(player.getRole())); // todo : à voir
	}

	protected void broadcastPlayerChange() {
		sendToAll(new SetPlayersPacket(config.getPlayers(), getPlayerList()));
	}

	public void sendToPlayers(Packet packet) {
		players.forEach(pl -> pl.sendPacket(packet));
	}

	public void sendToSpectators(Packet packet) {
		spectators.forEach(pl -> pl.sendPacket(packet));
	}

	public void sendToAll(Packet packet) {
		sendToPlayers(packet);
		sendToSpectators(packet);
	}

	public String[] getPlayerList() {
		List<String> lst = players.stream().map(GamePlayer::getName).collect(Collectors.toList());
		return lst.toArray(new String[lst.size()]);
	}

	public void sendToAll(Packet packet, Role role, Role... roles) {
		getPlayers(role, roles).forEach(pl -> pl.sendPacket(packet));
	}

	public Collection<GamePlayer> getPlayers(Role role, Role... roles) {
		List<Role> allRoles = Lists.newArrayList(roles);
		allRoles.add(role);
		return players.stream().filter(pl -> allRoles.contains(pl.getRole())).collect(Collectors.toList());
	}

	public List<GamePlayer> getPlayers() {
		return players;
	}

	public Collection<GamePlayer> getPlayersExcepted(Role role, Role... roles) {
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
		runPreparation(availableRoles);
	}

	public void runPreparation(List<Role> remaining) {
		Role[] available = new Role[2];
		if (remaining.size() >= 2) {
			available[0] = remaining.get(0);
			available[1] = remaining.get(1);
		} else if (remaining.size() == 1) {
			available[0] = remaining.get(0);
			available[1] = Role.VILLAGER;
		} else {
			available[0] = available[1] = Role.VILLAGER;
		}

		PreparationPhase phase = (PreparationPhase) Phases.getPhase(GamePhase.PREPARATION);
		phase.setRoles(available);
		phase.setRunAfter(this::runPreNight);
		phase.justAfter(electMayor());
		phase.run(this);
	}

	public boolean checkWin() {
		if (getPlayers(Role.WOLF).size() == 0) {
			sendToAll(new MessagePacket(MessageType.GAME, "Victoire du village !"));
			return true;
		} else if (getPlayersExcepted(Role.WOLF).size() == 0) {
			sendToAll(new MessagePacket(MessageType.GAME, "Victoire des Loups !"));
			return true;
		}
		return false;
	}

	public boolean hasToEnd() {
		if (state == GameState.FINISHED)
			return true;

		if (!checkWin())
			return false;

		TaskManager.submit(new DelayedTask(30) {
			@Override
			public void run() {
				GamesManager.removeGame(Game.this);
			}
		});
		setState(GameState.FINISHED);
		return true;
	}

	private void runPreNight() {
		if (hasToEnd())
			return;

		net.zyuiop.loupgarou.server.game.phases.GamePhase phase = Phases.getPhase(GamePhase.PRE_NIGHT);
		phase.setRunAfter(this::runNight);
		phase.run(this);
	}

	private void runNight() {
		if (hasToEnd())
			return;

		net.zyuiop.loupgarou.server.game.phases.GamePhase phase = Phases.getPhase(GamePhase.NIGHT);
		phase.setRunAfter(this::runEndNight);
		phase.run(this);
	}

	private void runEndNight() {
		if (hasToEnd())
			return;

		net.zyuiop.loupgarou.server.game.phases.GamePhase phase = Phases.getPhase(GamePhase.END_NIGHT);
		phase.setRunAfter(this::runDay);
		phase.run(this);
	}

	private void runDay() {
		if (hasToEnd())
			return;

		net.zyuiop.loupgarou.server.game.phases.GamePhase phase = Phases.getPhase(GamePhase.DAY);
		phase.setRunAfter(this::runPreNight);
		phase.run(this);
	}

	public void setState(GameState state) {
		this.state = state;
		sendToAll(new SetStatePacket(state));
	}

	public void setPhase(GamePhase phase) {
		this.phase = phase;
		sendToAll(new SetPhasePacket(phase));
	}

	public GamePlayer getNextVictim() {
		return nextVictim;
	}

	public void setNextVictim(GamePlayer nextVictim) {
		this.nextVictim = nextVictim;
	}

	public GamePlayer getMayor() {
		return mayor;
	}

	public void setMayor(GamePlayer mayor) {
		this.mayor = mayor;
	}

	private Task electMayor() {
		return new MajorityVote(60, "Votez pour un Capitaine", getPlayers(), getPlayerList()) {
			@Override
			public void run() {
				if (!hasToEnd())
					super.run();
				else
					complete();
			}

			@Override
			protected void maximalResults(Multimap<String, GamePlayer> maximal) {
				if (maximal.size() > 0) {
					String vote = maximal.keySet().iterator().next();
					GamePlayer mayor = GamePlayer.getPlayer(vote);
					if (mayor != null) {
						Game.this.mayor = mayor;
						sendToAll(new MessagePacket(MessageType.GAME, mayor.getName() + " est le nouveau capitaine de la ville !"));
					}
				}
			}
		};
	}

	public TaskChainer stumpPlayer(GamePlayer player, TaskChainer chainer) {
		// TODO : some stuff, mayor reelection
		players.remove(player);
		spectators.add(player);
		TaskManager.runAsync(this::broadcastPlayerChange);

		if (mayor == player) {
			chainer.justAfter(electMayor());
		}

		if (player.getRole() == Role.HUNTER) {
			chainer.justAfter(new HunterTask(this, chainer, player));
		}

		if (player.getLover() != null && players.contains(player.getLover())) {
			chainer.autoCompleteJustAfter(() -> {
				sendToAll(new MessagePacket(MessageType.GAME, player.getLover().getName() + " (" + player.getLover().getRole().getName() + ") était amoureux de " + player.getName() + " et se suicide !"));
				player.getLover().setLover(null);
				stumpPlayer(player.getLover(), chainer);
			});
		}

		return chainer;
	}

	public void addVictim(GamePlayer player) {
		otherVictims.add(player);
	}

	public List<GamePlayer> getEveryone() {
		List<GamePlayer> list = Lists.newArrayList(players);
		list.addAll(spectators);
		return list;
	}

	public Set<GamePlayer> getOtherVictims() {
		return otherVictims;
	}

	public int getGameId() {
		return gameId;
	}

	public void sendMessage(GamePlayer player, String message) {
		if (message.length() < 2)
			return;

		if (state == GameState.STARTED) {
			if (spectators.contains(player)) {
				sendToSpectators(new MessagePacket(MessageType.USER, player.getName(), message));
			} else if (players.contains(player)) {
				switch (phase) {
					case PREPARATION:
					case PRE_NIGHT:
					case END_NIGHT:
						player.sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous ne pouvez pas envoyer de message pendant cette phase de jeu !"));
						break;
					case NIGHT:
						if (player.getRole() == Role.WOLF) {
							sendToAll(new MessagePacket(MessageType.USER, player.getName(), message), Role.WOLF);
							sendToAll(new MessagePacket(MessageType.USER, "Loup", message), Role.LITTLE_GIRL);
						} else {
							player.sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous ne pouvez pas envoyer de message pendant cette phase de jeu !"));
						}
						break;
					case DAY:
						sendToAll(new MessagePacket(MessageType.USER, player.getName(), message));
						break;
				}
			}
		} else {
			sendToAll(new MessagePacket(MessageType.USER, player.getName(), message));
		}

		// TODO : check if player can send a message
		// TODO : send to some targets only

	}

	public void removePlayer(GamePlayer player) {
		players.remove(player);
		spectators.remove(player);
	}

	public String getProtectedPlayer() {
		return protectedPlayer;
	}

	public void setProtectedPlayer(String protectedPlayer) {
		this.protectedPlayer = protectedPlayer;
	}
}
