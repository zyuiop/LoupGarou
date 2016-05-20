package net.zyuiop.loupgarou.server.game.characters;

import com.google.common.collect.Lists;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.game.tasks.Task;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.roledata.WitchData;
import net.zyuiop.loupgarou.server.game.votes.Vote;

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
		if (game.isAncientDead())
			return;

		Collection<GamePlayer> witches = game.getPlayers(Role.WITCH);
		if (witches.size() > 0) {
			GamePlayer witch = witches.iterator().next();
			WitchData data = witch.getRoleData(WitchData.class);
			if (data == null) {
				complete();
				return;
			}

			game.sendToAll(new MessagePacket(MessageType.GAME, "La sorcière se réveille..."));

			List<Vote> votes = new ArrayList<>();
			GamePlayer dead = game.getNextVictim();

			String[] choices = new String[]{"Oui", "Non"};
			String voteName;

			if (data.isHealthPotionUsed()) {
				witch.sendMessage(MessageType.GAME, "Vous n'avez plus de potion de soin à utliser");
				choices = new String[]{"Plus de potion !"};
				voteName = "Pas de réanimation possible";
			} else if (dead == null) {
				witch.sendMessage(MessageType.GAME, "Il n'y a personne à sauver cette nuit.");
				voteName = "Aucun mort cette nuit";
				choices = new String[]{"Aucun mort !"};
			} else {
				voteName = "Réanimer " + dead.getName() + " ?";
			}

			// On affiche quand même le vote
			votes.add(new Vote(45, voteName, witches, choices) {
				@Override
				protected void handleResults(Map<GamePlayer, String> results) {
					if (data.isHealthPotionUsed() || dead == null) {
						return;
					}

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


			List<String> names;
			if (data.isKillPotionUsed()) {
				witch.sendMessage(MessageType.GAME, "Vous n'avez plus de potion de mort à utliser");
				voteName = "Pas de meurtre possible";
				names = Lists.newArrayList("Plus de potion !");
			} else {
				voteName = "Tuer un joueur ?";
				names = game.getPlayersExcepted(Role.WITCH).stream().map(GamePlayer::getName).collect(Collectors.toList());
				names.add("Personne");
			}


			if (dead != null) {
				names.remove(dead.getName());
			}

			if (names.size() > 0) {
				votes.add(new Vote(45, voteName, witches, names) {
					@Override
					protected void handleResults(Map<GamePlayer, String> results) {
						if (data.isKillPotionUsed())
							return;

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

			if (votes.size() == 0) {
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

			task.setRunAfter(() -> {
				game.sendToAll(new MessagePacket(MessageType.GAME, "La sorcière se rendort..."));
				WitchCharacter.this.complete();
			});
			task.run();
		} else {
			complete();
		}
	}
}
