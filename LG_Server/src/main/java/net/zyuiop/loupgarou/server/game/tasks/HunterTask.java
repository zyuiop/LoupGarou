package net.zyuiop.loupgarou.server.game.tasks;

import com.google.common.collect.Lists;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.votes.Vote;
import net.zyuiop.loupgarou.server.tasks.Task;
import net.zyuiop.loupgarou.server.tasks.TaskChainer;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class HunterTask extends Task {
	private final Game game;
	private final TaskChainer chainer;
	private final GamePlayer hunter;

	public HunterTask(Game game, TaskChainer chainer, GamePlayer hunter) {
		this.game = game;
		this.chainer = chainer;
		this.hunter = hunter;
	}

	@Override
	public void run() {
		if (hunter == null) {
			complete();
			return;
		}

		Vote vote = new Vote(45, "Qui souhaitez vous éliminer ?", Lists.newArrayList(hunter), game.getPlayersExcepted(Role.HUNTER).stream().map(GamePlayer::getName).collect(Collectors.toList())) {
			@Override
			protected void handleResults(Map<GamePlayer, String> results) {
				if (results.size() == 0) {
					hunter.sendMessage(MessageType.GAME, "Vous n'avez choisi aucune cible...");
				} else {
					String target = results.values().iterator().next();
					GamePlayer player = GamePlayer.getPlayer(target);
					if (player == null || player.getGame() != game) {
						hunter.sendMessage(MessageType.GAME, "Le joueur choisi semble ne plus exister...");
						LGServer.getLogger().info("Error in game " + game.getGameInfo().getGameName() + " ! Player designated " + target + " is no longer playing !");
					} else {
						game.sendToAll(new MessagePacket(MessageType.GAME, hunter.getName() + " était chasseur et décide de tuer " + player.getName() + " (" + player.getRole().getName() + ") dans un dernier soupir..."));
						game.stumpPlayer(player, chainer);
					}
				}
			}
		};

		vote.setRunAfter(HunterTask.this::complete);

		vote.run();
	}
}
