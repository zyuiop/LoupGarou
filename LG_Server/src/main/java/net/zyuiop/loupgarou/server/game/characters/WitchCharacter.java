package net.zyuiop.loupgarou.server.game.characters;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.roledata.WitchData;
import net.zyuiop.loupgarou.server.game.votes.Vote;
import net.zyuiop.loupgarou.game.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class WitchCharacter extends Character {
	protected WitchCharacter(Game game) {
		super(game);
	}

	@Override
	public void run() {
		Collection<GamePlayer> witches = game.getPlayers(Role.WITCH);
		if (witches.size() > 0) {
			GamePlayer witch = witches.iterator().next();
			WitchData data = witch.getRoleData(WitchData.class);
			if (data == null) {
				complete();
				return;
			}

			List<Vote> votes = new ArrayList<>();
			if (!data.isHealthPotionUsed()) {
				GamePlayer dead = game.getNextVictim();
				if (dead != null) {
					votes.add(new Vote(30, "Réanimer " + dead.getName() + " ?", witches, new String[]{"Oui", "Non"}) {
						@Override
						protected void handleResults(Map<GamePlayer, String> results) {
							if (results.size() > 0 && results.values().iterator().next().equalsIgnoreCase("oui")) {
								// Utilisation de la potion de vie
								game.setNextVictim(null);
								data.setHealthPotionUsed(true);
								witch.sendMessage(MessageType.GAME, "Vous avez sauvé " + dead.getName() + " !");
							} else {
								witch.sendMessage(MessageType.GAME, "Vous choisissez de ne pas sauver " + dead.getName() + ".");
							}
						}
					});
				} else {
					witch.sendMessage(MessageType.GAME, "Il n'y a personne à sauver cette nuit.");
				}
			}

			if (!data.isKillPotionUsed()) {
				GamePlayer dead = game.getNextVictim();
				List<String> names = game.getPlayersExcepted(Role.WITCH).stream().map(GamePlayer::getName).collect(Collectors.toList());
				if (dead != null) {
					names.remove(dead.getName());
				}

				if (names.size() > 0) {
					names.add("Personne");
					votes.add(new Vote(30, "Tuer une personne", witches, names) {
						@Override
						protected void handleResults(Map<GamePlayer, String> results) {
							if (results.size() > 0) {
								String target = results.values().iterator().next();
								if (!target.equalsIgnoreCase("Personne")) {
									GamePlayer player = GamePlayer.getPlayer(target);
									if (player == null || player.getGame() != game) {
										witch.sendMessage(MessageType.GAME, "Une erreur s'est produite. Vous ne tuez personne ce tour ci.");
									} else {
										data.setKillPotionUsed(true);
										game.addVictim(player);
									}
								} else {
									witch.sendMessage(MessageType.GAME, "Vous ne tuez personne ce tour ci.");
								}
							} else {
								witch.sendMessage(MessageType.GAME, "Vous ne tuez personne ce tour ci.");
							}
						}
					});
				}
			}

			if (votes.size() == 0) {
				witch.sendMessage(MessageType.GAME, "Vous avez utilisé toutes vos potions.");
				complete();
				return;
			}

			Task task = new Task() {
				private List<Task> remaining = new ArrayList<>(votes);

				@Override
				public void run() {
					votes.forEach(vote -> {
						vote.setRunAfter(() -> complete(vote));
						vote.run();
					});
				}

				private void complete(Task task) {
					remaining.remove(task);
					if (remaining.size() == 0)
						complete();
				}
			};

			task.setRunAfter(WitchCharacter.this::complete);
			task.run();
		} else {
			complete();
		}
	}
}
