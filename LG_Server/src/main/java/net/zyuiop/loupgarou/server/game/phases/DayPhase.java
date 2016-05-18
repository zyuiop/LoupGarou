package net.zyuiop.loupgarou.server.game.phases;

import com.google.common.collect.Multimap;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.votes.MajorityVote;
import net.zyuiop.loupgarou.server.game.votes.Vote;
import net.zyuiop.loupgarou.server.tasks.TaskChainer;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class DayPhase extends GamePhase {
	DayPhase() {
		super(net.zyuiop.loupgarou.game.GamePhase.DAY);
	}

	@Override
	protected void invoke(Game game) {
		List<GamePlayer> victims = new ArrayList<>();
		// TODO : récup autres victimes

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

		for (GamePlayer stump : victims)
			game.stumpPlayer(stump, this);

		autoComplete(() -> game.sendToAll(new MessagePacket(MessageType.GAME, "Il est maintenant temps de choisir qui le village va éliminer aujourd'hui.")));
		next(() -> {
			List<String> choices = game.getPlayers().stream().map(GamePlayer::getName).collect(Collectors.toList());
			choices.add("Personne");

			Vote vote = new MajorityVote(300, "Désignez un coupable", game.getPlayers(), choices) {
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
							game.sendToAll(new MessagePacket(MessageType.GAME, "La victime désignée est " + designated + " !"));
							game.stumpPlayer(player, DayPhase.this);
						}
					}
				}
			};

			vote.setRunAfter(this::complete);

			vote.run();
		});
	}
}
