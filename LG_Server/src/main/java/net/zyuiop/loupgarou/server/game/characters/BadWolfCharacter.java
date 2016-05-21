package net.zyuiop.loupgarou.server.game.characters;

import com.google.common.collect.Lists;
import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.roledata.WhiteWolfData;
import net.zyuiop.loupgarou.server.game.votes.Vote;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 *
 * The Parting of The Ways
 */
public class BadWolfCharacter extends Character {
	protected BadWolfCharacter(Game game) {
		super(game);
	}

	@Override
	public void run() {
		Collection<GamePlayer> wolves = game.getPlayers(Role.GREAT_BAD_WOLF);
		if (wolves.size() == 0) {
			complete();
			return;
		}

		GamePlayer wolf = wolves.iterator().next();
		if (game.isWolfKilled()) {
			wolf.sendMessage(MessageType.GAME, "Vous ne vous réveillez pas à nouveau cette nuit car un loup garou a déjà été tué...");
			complete();
			return;
		}

		List<String> available = Lists.newArrayList("Personne");
		available.addAll(game.getAllExceptedWolves().stream().map(GamePlayer::getName).collect(Collectors.toList()));
		game.sendToAll(new MessagePacket(MessageType.GAME, "Le Grand Méchant Loup se réveille..."));

		Vote vote = new Vote(45, "Désignez votre victime", wolves, available) {
			@Override
			protected void handleResults(Map<GamePlayer, String> results) {
				if (available.size() == 1)
					return;

				if (results.size() == 0) {
					wolf.sendMessage(MessageType.GAME, "Vous n'avez désigné aucun joueur à tuer cette nuit.");
				} else {
					String value = results.values().iterator().next();
					if (value.equalsIgnoreCase("personne")) {
						wolf.sendMessage(MessageType.GAME, "Vous n'avez désigné aucun joueur à tuer cette nuit.");
					} else {
						GamePlayer player = GamePlayer.getPlayer(value);
						if (player == null) {
							wolf.sendMessage(MessageType.GAME, "Une erreur s'est produite, aucun joueur ne mourra cette nuit.");
						} else {
							// Salvateur. Sans effet sur le GML ?
							if (game.getProtectedPlayer() == null || !game.getProtectedPlayer().equalsIgnoreCase(player.getName()))
								game.addVictim(player);
							wolf.sendMessage(MessageType.GAME, "Vous avez bien tué " + player.getName() + ".");
						}
					}
				}
			}
		};

		vote.setRunAfter(() -> {
			game.sendToAll(new MessagePacket(MessageType.GAME, "Le Grand Méchant Loup se rendort..."));
			BadWolfCharacter.this.complete();
		});
		vote.run();
	}
}
