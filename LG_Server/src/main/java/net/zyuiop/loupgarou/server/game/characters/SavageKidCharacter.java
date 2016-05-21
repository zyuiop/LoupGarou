package net.zyuiop.loupgarou.server.game.characters;

import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.roledata.RoleData;
import net.zyuiop.loupgarou.server.game.votes.Vote;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class SavageKidCharacter extends Character {
	protected SavageKidCharacter(Game game) {
		super(game);
	}

	@Override
	public void run() {
		if (game.isAncientDead()) {
			complete();
			return;
		}

		Collection<GamePlayer> kids = game.getPlayers(Role.SAVAGE_KID);
		if (kids.size() == 0) {
			complete();
			return;
		}

		GamePlayer kid = kids.iterator().next();

		game.sendToAll(new MessagePacket(MessageType.GAME, "L'enfant sauvage choisit son modèle..."));

		Vote vote = new Vote(60, "Choisissez votre modèle", kids, game.getPlayersExcepted(Role.SAVAGE_KID).stream().map(GamePlayer::getName).collect(Collectors.toList())) {
			@Override
			protected void handleResults(Map<GamePlayer, String> results) {
				if (results.size() == 0) {
					kid.sendMessage(MessageType.GAME, "Vous n'avez pas choisi de modèle, vous êtes donc villageois.");
					kid.setRole(Role.VILLAGER);
				} else {
					String target = results.values().iterator().next();
					GamePlayer player = GamePlayer.getPlayer(target);
					if (player == null || player.getGame() != game) {
						kid.sendMessage(MessageType.GAME, "Erreur sur le joueur sélectionné, vous devenez villageois.");
						kid.setRole(Role.VILLAGER);
						LGServer.getLogger().info("Error in game " + game.getGameInfo().getGameName() + " ! Player designated " + target + " is no longer playing !");
					} else {
						RoleData data = player.getRoleData(RoleData.class);
						if (data != null) {
							data.setModelOf(kid);
							kid.sendMessage(MessageType.GAME, "Vous avez choisi votre modèle. Si celui ci meurt, vous deviendrez loup garou.");
						}
					}
				}
			}
		};

		vote.setRunAfter(() -> {
			game.sendToAll(new MessagePacket(MessageType.GAME, "L'enfant sauvage a choisi son modèle."));
			SavageKidCharacter.this.complete();
		});

		vote.run();
	}
}
