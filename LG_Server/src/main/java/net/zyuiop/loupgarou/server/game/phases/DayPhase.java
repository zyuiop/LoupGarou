package net.zyuiop.loupgarou.server.game.phases;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.protocol.utils.MessageModifier;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.roledata.IdiotRoleData;
import net.zyuiop.loupgarou.server.game.votes.MajorityVote;
import net.zyuiop.loupgarou.server.game.votes.Vote;
import net.zyuiop.loupgarou.protocol.threading.Task;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class DayPhase extends GamePhase {
	DayPhase() {
		super(net.zyuiop.loupgarou.protocol.data.GamePhase.DAY);
	}

	@Override
	protected void invoke(Game game) {
		List<GamePlayer> victims = Lists.newArrayList(game.getOtherVictims());
		if (game.getNextVictim() != null) {
			victims.add(game.getNextVictim());
			game.setNextVictim(null);
		}

		autoComplete(() -> {
			if (victims.size() == 0)
				game.sendToAll(new MessagePacket(MessageType.GAME, "Le village se réveille, et personne n'est mort cette nuit !"));
			else
				game.sendToAll(new MessagePacket(MessageType.GAME, "Le village se réveille, sauf " +
						StringUtils.join(victims.stream().map(player -> player.getName() + " (" + player.getRole().getName() + ")").collect(Collectors.toList()), ", ") +
						", mort" + (victims.size() > 1 ? "s" : "") + " dans la nuit."));
		});

		next(new Task() {
			@Override
			public void run() {
				for (GamePlayer stump : victims)
					game.stumpPlayer(stump, DayPhase.this);

				complete();
			}
		});

		next(new Task() {
			@Override
			public void run() {
				if (game.hasToEnd()) {
					DayPhase.this.complete();
					return;
				}
				complete();
			}
		});

		autoComplete(() -> game.sendToAll(new MessagePacket(MessageType.GAME, "Il est maintenant temps de choisir qui le village va éliminer aujourd'hui.")));
		next(new Task() {
			@Override
			public void run() {
				List<String> choices = game.getPlayers().stream().map(GamePlayer::getName).collect(Collectors.toList());
				Collection<GamePlayer> voters = game.getPlayers();
				Collection<GamePlayer> idiots = game.getPlayers(Role.IDIOT);
				if (idiots.size() != 0) {
					GamePlayer idiot = idiots.iterator().next();
					IdiotRoleData data = idiot.getRoleData(IdiotRoleData.class);
					if (data != null && data.isDead()) {
						choices.remove(idiot.getName());
						voters.remove(idiot);
					}
				}
				choices.add("Personne");

				Vote vote = new MajorityVote(180, "Désignez un coupable", voters, choices) {
					@Override
					protected void handleResults(Map<GamePlayer, String> results) {
						Iterator<String> values = results.values().iterator();
						while (values.hasNext())
							if (values.next().equalsIgnoreCase("Personne"))
								values.remove();

						super.handleResults(results);
					}

					@Override
					protected void maximalResults(Multimap<String, GamePlayer> maximal) {
						String designated = null;
						if (maximal.size() == 0) {
							game.sendToAll(new MessagePacket(MessageType.GAME, "Le village n'a pas désigné de coupable aujourd'hui, personne ne meurt !"));
							return;
						} else if (maximal.keySet().size() > 1) {
							GamePlayer mayor = game.getMayor();
							if (mayor != null) {
								for (String option : maximal.keySet()) {
									if (maximal.get(option).contains(mayor)) {
										designated = option;
										break;
									}
								}
							}

							if (designated == null) {
								game.sendToAll(new MessagePacket(MessageType.GAME, "Égalité sur les votes, personne ne sera éliminé !"));
								return;
							}
						} else {
							designated = maximal.keySet().iterator().next();
						}

						if (designated != null) {
							GamePlayer player = GamePlayer.getPlayer(designated);
							if (player == null || player.getGame() != game) {
								game.sendToAll(new MessagePacket(MessageType.GAME, "Le joueur désigné ne semble plus exister... Étrange..."));
								LGServer.getLogger().info("Error in game " + game.getGameInfo().getGameName() + " ! Player designated " + designated + " is no longer playing !");
							} else {
								Role role = player.getRole();
								game.sendToAll(new MessagePacket(MessageType.GAME, "La victime désignée est " + designated + " (" + role.getName() + ") !"));
								if (role == Role.ANCIENT) {
									game.sendToAll(new MessagePacket(MessageType.GAME, "Vous avez tué l'ancien ! Vous perdez tous vos pouvoirs.", MessageModifier.BOLD, (short) 0xB9, (short) 0x12, (short) 0x1B));
									game.setAncientDead(true);
								} else if (role == Role.ANGEL && game.checkAngel()) {
									game.sendToAll(new MessagePacket(MessageType.GAME, "Vous avez tué l'ange ! Celui ci gagne la partie.", MessageModifier.BOLD, (short) 0xB9, (short) 0x12, (short) 0x1B));
									player.sendPacket(new MessagePacket(MessageType.GAME, "Vous remportez la partie !", MessageModifier.BOLD, (short) 0x8F, (short) 0xCF, (short) 0x3C));
									game.win();
									return;
								} else if (role == Role.IDIOT) {
									IdiotRoleData data = player.getRoleData(IdiotRoleData.class);
									if (data != null) {
										data.villageKill();
										game.sendToAll(new MessagePacket(MessageType.GAME, "Le joueur était l'idiot du village. Il ne meurt pas mais ne pourra désormais plus voter.", MessageModifier.BOLD, (short) 0xB9, (short) 0x12, (short) 0x1B));
									}
									return;
								}
								game.stumpPlayer(player, DayPhase.this);
							}
						}
					}
				};

				vote.setRunAfter(this::complete);

				vote.run();
			}
		});
	}
}
