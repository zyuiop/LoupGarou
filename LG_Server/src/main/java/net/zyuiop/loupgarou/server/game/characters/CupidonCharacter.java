package net.zyuiop.loupgarou.server.game.characters;

import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.protocol.threading.Task;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.votes.CupidonVote;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class CupidonCharacter extends Character {
	protected CupidonCharacter(Game game) {
		super(game);
	}

	@Override
	public void run() {
		if (game.isAncientDead()) {
			complete();
			return;
		}

		Collection<GamePlayer> cupidon = game.getPlayers(Role.CUPIDON);
		if (cupidon.size() > 0) {
			Collection<String> others = game.getPlayers().stream().map(GamePlayer::getName).collect(Collectors.toList());
			String[] choices = others.toArray(new String[others.size()]);

			game.sendToAll(new MessagePacket(MessageType.GAME, "Cupdion se réveille et désigne les deux amoureux..."));

			CupidonVote first = new CupidonVote(60, "Premier amoureux", cupidon, choices);
			CupidonVote second = new CupidonVote(60, "Second amoureux", cupidon, choices);

			first.setOtherVote(second);
			second.setOtherVote(first);

			Task task = new Task() {
				private boolean firstOk = false;
				private boolean secondOk = false;

				@Override
				public void run() {
					first.run();
					first.setRunAfter(this::completeFirst);
					second.run();
					second.setRunAfter(this::completeSecond);
				}

				private void completeFirst() {
					firstOk = true;
					if (secondOk)
						complete();
				}

				private void completeSecond() {
					secondOk = true;
					if (firstOk)
						complete();
				}
			};
			task.setRunAfter(() -> {
				game.sendToAll(new MessagePacket(MessageType.GAME, "Cupidon se rendort..."));
				CupidonCharacter.this.complete();
			});
			task.run();
		} else {
			complete();
		}
	}
}
