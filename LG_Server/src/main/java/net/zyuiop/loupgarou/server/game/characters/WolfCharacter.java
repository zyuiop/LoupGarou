package net.zyuiop.loupgarou.server.game.characters;

import com.google.common.collect.Multimap;
import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.roledata.AncientData;
import net.zyuiop.loupgarou.server.game.votes.MajorityVote;
import net.zyuiop.loupgarou.server.game.votes.Vote;

import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class WolfCharacter extends Character {
	protected WolfCharacter(Game game) {
		super(game);
	}

	@Override
	public void run() {
		game.sendToAll(new MessagePacket(MessageType.GAME, "Les loups garous se réveillent..."));
		game.sendToWolves(new MessagePacket(MessageType.GAME, "Vous êtes un loup garou : vous allez pouvoir désigner une victime."));

		Vote vote = new MajorityVote(45, "Désignez votre victime", game.getWolves(), game.getAllExceptedWolves().stream().map(GamePlayer::getName).collect(Collectors.toList())) {
			@Override
			protected void maximalResults(Multimap<String, GamePlayer> maximal) {
				if (maximal.size() == 0) {
					game.sendToWolves(new MessagePacket(MessageType.GAME, "Aucun joueur désigné, tout le monde vivra !"));
				} else if (maximal.keySet().size() > 1) {
					game.sendToWolves(new MessagePacket(MessageType.GAME, "Égalité sur les votes, personne ne mourra cette nuit !"));
				} else {
					String victim = maximal.keySet().iterator().next();
					GamePlayer player = GamePlayer.getPlayer(victim);
					if (player == null || player.getGame() != game) {
						game.sendToWolves(new MessagePacket(MessageType.GAME, "Le joueur désigné ne semble plus exister... Étrange..."));
						LGServer.getLogger().info("Error in game " + game.getGameInfo().getGameName() + " ! Player designated " + victim + " is no longer playing !");
					} else {
						game.sendToWolves(new MessagePacket(MessageType.GAME, "La victime désignée est " + victim + " !"));
						boolean shouldKill = game.getProtectedPlayer() == null || !game.getProtectedPlayer().equalsIgnoreCase(victim);
						if (shouldKill) {
							if (player.getRole() == Role.ANCIENT) {
								AncientData data = player.getRoleData(AncientData.class);
								if (!data.tryToKill())
									shouldKill = false;
							}
						}

						if (shouldKill)
							game.setNextVictim(player);

						game.setProtectedPlayer(null);
					}
				}
			}
		};

		vote.setRunAfter(() -> {
			game.sendToAll(new MessagePacket(MessageType.GAME, "Les loups garous se rendorment."));
			WolfCharacter.this.complete();
		});

		vote.run();
	}
}
