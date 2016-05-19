package net.zyuiop.loupgarou.server.game.characters;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.votes.Vote;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class MediumCharacter extends Character {
	protected MediumCharacter(Game game) {
		super(game);
	}

	@Override
	public void run() {
		if (game.isAncientDead())
			return;

		Collection<GamePlayer> mediums = game.getPlayers(Role.MEDIUM);
		if (mediums.size() == 0) {
			complete();
			return;
		}

		GamePlayer medium = mediums.iterator().next();
		Vote vote = new Vote(45, "Qui souhaitez vous observer ?", mediums, game.getPlayersExcepted(Role.MEDIUM).stream().map(GamePlayer::getName).collect(Collectors.toList())) {
			@Override
			protected void handleResults(Map<GamePlayer, String> results) {
				if (results.size() == 0) {
					medium.sendMessage(MessageType.GAME, "Vous n'avez choisi personne à observer...");
				} else {
					String target = results.values().iterator().next();
					GamePlayer player = GamePlayer.getPlayer(target);
					if (player == null || player.getGame() != game) {
						medium.sendMessage(MessageType.GAME, "Le joueur choisi semble ne plus exister...");
						LGServer.getLogger().info("Error in game " + game.getGameInfo().getGameName() + " ! Player designated " + target + " is no longer playing !");
					} else {
						Role role = player.getRole();
						if (role != null)
							medium.sendMessage(MessageType.GAME, "Le joueur désigné joue la classe " + role.getName());
						else
							medium.sendMessage(MessageType.GAME, "Ce joueur n'a pas de rôle...");
					}
				}
			}
		};

		vote.setRunAfter(MediumCharacter.this::complete);

		vote.run();
	}
}
