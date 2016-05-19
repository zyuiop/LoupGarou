package net.zyuiop.loupgarou.server.game.characters;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.roledata.RoleDatas;
import net.zyuiop.loupgarou.server.game.roledata.WhiteWolfData;
import net.zyuiop.loupgarou.server.game.votes.MajorityVote;
import net.zyuiop.loupgarou.server.game.votes.Vote;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class WhiteWolfCharacter extends Character {
	protected WhiteWolfCharacter(Game game) {
		super(game);
	}

	@Override
	public void run() {
		Collection<GamePlayer> wolves = game.getPlayers(Role.WHITE_WOLF);
		if (wolves.size() == 0) {
			complete();
			return;
		}

		GamePlayer wolf = wolves.iterator().next();
		WhiteWolfData data = wolf.getRoleData(WhiteWolfData.class);
		if (data == null)
			return;
		if (!data.passTurn())
			return;

		List<String> available = Lists.newArrayList("Personne");
		available.addAll(game.getPlayers(Role.WOLF).stream().map(GamePlayer::getName).collect(Collectors.toList()));

		if (available.size() == 1) {
			wolf.sendMessage(MessageType.GAME, "Vous êtes le seul loup garou en vie, vous ne tuerez donc personne ce soir.");
			return;
		}

		wolf.sendMessage(MessageType.GAME, "Vous êtes loup garou blanc, vous pouvez donc choisir de tuer un loup garou ce soir.");

		Vote vote = new Vote(45, "Désignez votre victime", wolves, available) {
			@Override
			protected void handleResults(Map<GamePlayer, String> results) {
				if (results.size() == 0) {
					wolf.sendMessage(MessageType.GAME, "Vous n'avez désigné aucun loup à tuer cette nuit.");
				} else {
					String value = results.values().iterator().next();
					if (value.equalsIgnoreCase("personne")) {
						wolf.sendMessage(MessageType.GAME, "Vous n'avez désigné aucun loup à tuer cette nuit.");
					} else {
						GamePlayer player = GamePlayer.getPlayer(value);
						if (player == null) {
							wolf.sendMessage(MessageType.GAME, "Une erreur s'est produite, aucun loup ne mourra cette nuit.");
						} else {
							game.addVictim(player);
							wolf.sendMessage(MessageType.GAME, "Vous avez bien tué " + player.getName() + ".");
						}
					}
				}
			}
		};

		vote.setRunAfter(WhiteWolfCharacter.this::complete);
		vote.run();
	}
}