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
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.phases.Phases;
import net.zyuiop.loupgarou.server.game.phases.PreparationPhase;
import net.zyuiop.loupgarou.server.game.tasks.HunterTask;
import net.zyuiop.loupgarou.server.game.votes.MajorityVote;
import net.zyuiop.loupgarou.server.game.votes.Vote;
import net.zyuiop.loupgarou.server.utils.TaskChainer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class Game {
	private final int gameId;
	private       GameState                    state      = GameState.WAITING;
	private       GamePhase                    phase      = null;
	private       Set<GamePlayer>              players    = new HashSet<>();
	private       Set<GamePlayer>              spectators = new HashSet<>();
	private final Map<GamePlayer, DelayedTask> leaveTasks = new HashMap<>();
	private GameConfig config;

	// Running variables
	private GamePlayer      nextVictim      = null;
	private Set<GamePlayer> otherVictims    = Sets.newHashSet();
	private GamePlayer      mayor           = null;
	private String          protectedPlayer = null;
	private boolean         isAncientDead   = false;

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
				confirmJoin(player);
				player.setGame(this);
				player.sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous avez rejoint la partie !"));
				sendToAll(new MessagePacket(MessageType.SYSTEM, player.getName() + " a rejoint la partie !"));

				if (players.size() >= config.getPlayers())
					start();
			} else {
				confirmJoin(player);
			}
		} else {
			if (!players.contains(player)) {
				confirmJoin(player);
				spectators.add(player);
				player.getClient().sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous rejoignez en tant que spectateur !"));
			} else {
				if (leaveTasks.containsKey(player)) {
					confirmJoin(player);
					leaveTasks.remove(player).cancel();
					Vote.reconnect(player);
					sendToAll(new MessagePacket(MessageType.SYSTEM, player.getName() + " s'est reconnecté !"));
				} else {
					LGServer.getLogger().warn("Player reconnected but was not expected (" + player.getName() + ", expected " + leaveTasks.keySet().toString() + ")");
				}
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
		player.sendPacket(new SetGameCompositionPacket(config.getExtendedCharacters()));
		if (state == GameState.STARTED)
			player.sendPacket(new SetRolePacket(player.getRole())); // todo : à voir
	}

	protected void broadcastPlayerChange() {
		sendToAll(new SetPlayersPacket(config.getPlayers(), getPlayerList()));
	}

	protected void broadcastCompositionChange() {
		sendToAll(new SetGameCompositionPacket(config.getExtendedCharacters()));
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

	public Set<GamePlayer> getPlayers() {
		return players;
	}

	public Collection<GamePlayer> getPlayersExcepted(Role role, Role... roles) {
		List<Role> allRoles = Lists.newArrayList(roles);
		allRoles.add(role);
		return players.stream().filter(player -> !allRoles.contains(player.getRole())).collect(Collectors.toList());
	}

	public GameInfo getGameInfo() {
		return new GameInfo(gameId, config.getName(), config.getHoster(), state, players.size(), config.getPlayers(), config.getPassword() != null);
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
		if (getPlayers(Role.WHITE_WOLF).size() == 1 && getPlayersExcepted(Role.WHITE_WOLF).size() < 2) {
			sendToAll(new MessagePacket(MessageType.GAME, "Victoire du Loup Blanc !"));
			return true;
		} else if (getPlayers(Role.WOLF, Role.WHITE_WOLF).size() == 0) {
			sendToAll(new MessagePacket(MessageType.GAME, "Victoire du village !"));
			return true;
		} else if (getPlayersExcepted(Role.WOLF, Role.WHITE_WOLF).size() == 0) {
			sendToAll(new MessagePacket(MessageType.GAME, "Victoire des Loups !"));
			sendToAll(new MessagePacket(MessageType.GAME, "Vous perdez la partie ! (victoire des loups)"), Role.WHITE_WOLF);
			return true;
		} else {
			List<GamePlayer> players = Lists.newArrayList(this.players);
			if (players.size() == 2 && players.get(0).getLover() != null && players.get(0).getLover().equals(players.get(1))) {
				String p1 = players.get(0) + " (" + players.get(0).getRole().getName() + ")";
				String p2 = players.get(1) + " (" + players.get(1).getRole().getName() + ")";

				sendToAll(new MessagePacket(MessageType.GAME, "Victoire des deux amoureux : " + p1 + " et " + p2));
				return true;
			}
		}
		return false;
	}

	public boolean hasToEnd() {
		if (state == GameState.FINISHED)
			return true;

		if (!checkWin())
			return false;

		win();
		return true;
	}

	public void win() {
		if (state == GameState.FINISHED)
			return;

		TaskManager.submit(new DelayedTask(30) {
			@Override
			public void run() {
				GamesManager.removeGame(Game.this);
			}
		});
		setState(GameState.FINISHED);
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

		if (player.getRole() == Role.HUNTER && player.getClient() != null && player.getGame() == this) {
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
				sendToSpectators(new MessagePacket(MessageType.USER, "[Spec] " + player.getName(), message));
			} else if (players.contains(player)) {
				switch (phase) {
					case PRE_NIGHT:
					case END_NIGHT:
						player.sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous ne pouvez pas envoyer de message pendant cette phase de jeu !"));
						break;
					case NIGHT:
						if (player.getRole() == Role.WOLF || player.getRole() == Role.WHITE_WOLF) {
							sendToAll(new MessagePacket(MessageType.USER, "[Loups] " + player.getName(), message), Role.WOLF, Role.WHITE_WOLF);
							sendToAll(new MessagePacket(MessageType.USER, "Loup", message), Role.LITTLE_GIRL);
						} else {
							player.sendPacket(new MessagePacket(MessageType.SYSTEM, "Vous ne pouvez pas envoyer de message pendant cette phase de jeu !"));
						}
						break;
					case DAY:
					case PREPARATION:
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

	public void leaveRoom(GamePlayer player) {
		if (state == GameState.STARTED && players.contains(player)) {
			synchronized (leaveTasks) {
				DelayedTask task = leaveTasks.remove(player);
				if (task != null)
					task.cancel();
			}

			TaskChainer chainer = new TaskChainer("AutoStumpChainer");
			player.setGame(null);
			sendToAll(new MessagePacket(MessageType.GAME, player.getName() + " est éliminé (déconnexion). Il était " + player.getRole().getName()));

			stumpPlayer(player, chainer);
			chainer.autoComplete(this::checkWin);
			chainer.run();
			return;
		} else {
			players.remove(player);
			spectators.remove(player);
			broadcastPlayerChange();
			sendToAll(new MessagePacket(MessageType.SYSTEM, player.getName() + " a quitté la partie."));
		}
	}

	public void removePlayer(GamePlayer player) {
		if (state == GameState.STARTED && players.contains(player)) {
			DelayedTask task = new DelayedTask(300) {
				@Override
				public void run() {
					leaveRoom(player);
				}
			};

			if (leaveTasks.containsKey(player))
				leaveTasks.remove(player).cancel();

			leaveTasks.put(player, task);
			TaskManager.submit(task);
			sendToAll(new MessagePacket(MessageType.SYSTEM, player.getName() + " s'est déconnecté. Il sera éliminé dans 5 minutes."));
			return;
		}

		sendToAll(new MessagePacket(MessageType.SYSTEM, player.getName() + " a quitté la partie."));

		players.remove(player);
		spectators.remove(player);
		broadcastPlayerChange();
	}

	public String getProtectedPlayer() {
		return protectedPlayer;
	}

	public void setProtectedPlayer(String protectedPlayer) {
		this.protectedPlayer = protectedPlayer;
	}

	public boolean isAncientDead() {
		return isAncientDead;
	}

	public void setAncientDead(boolean ancientDead) {
		isAncientDead = ancientDead;
	}

	private boolean firstTurn = true;

	public boolean checkAngel() {
		if (firstTurn) {
			firstTurn = false;
			return true;
		}
		return false;
	}

	public boolean checkPassword(String password) {
		return config.getPassword() == null || config.getPassword().equals(password);
	}

	public GameState getState() {
		return state;
	}

	public void setConfig(GameConfig config) {
		this.config = config;
		broadcastPlayerChange();
		broadcastCompositionChange();

		if (config.getPlayers() <= players.size()) {
			start();
		}
	}

	public GameConfig getConfig() {
		return config;
	}
}
