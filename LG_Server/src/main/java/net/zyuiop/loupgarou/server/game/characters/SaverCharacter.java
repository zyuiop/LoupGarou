package net.zyuiop.loupgarou.server.game.characters;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.game.tasks.Task;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.roledata.SaverData;
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
public class SaverCharacter extends Character {
	protected SaverCharacter(Game game) {
		super(game);
	}

	@Override
	public void run() {
		if (game.isAncientDead())
			return;

		Collection<GamePlayer> savers = game.getPlayers(Role.SAVER);
		if (savers.size() > 0) {
			GamePlayer saver = savers.iterator().next();
			SaverData data = saver.getRoleData(SaverData.class);
			if (data == null) {
				complete();
				return;
			}

			Vote vote = new Vote(45, "Qui souhaitez vous protéger ?", savers, game
					.getPlayers()
					.stream()
					.filter(player -> data.getLastSaved() == null || !player.getName().equalsIgnoreCase(data.getLastSaved()))
					.map(GamePlayer::getName)
					.collect(Collectors.toList())) {
				@Override
				protected void handleResults(Map<GamePlayer, String> results) {
					if (results.size() == 0) {
						saver.sendMessage(MessageType.GAME, "Vous ne protégez personne cette nuit.");
					} else {
						String name = results.values().iterator().next();
						game.setProtectedPlayer(name);
						saver.sendMessage(MessageType.GAME, "Vous protégez " + name + " !");
					}
				}
			};

			vote.setRunAfter(SaverCharacter.this::complete);

			vote.run();
		} else {
			complete();
		}
	}
}
