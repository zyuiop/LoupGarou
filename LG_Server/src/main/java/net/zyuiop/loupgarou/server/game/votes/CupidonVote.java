package net.zyuiop.loupgarou.server.game.votes;

import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;

import java.util.Collection;
import java.util.Map;

/**
 * @author zyuiop
 */
public class CupidonVote extends Vote {
	private CupidonVote otherVote = null;
	private String finallySelected = null;

	public CupidonVote(int time, String name, Collection<GamePlayer> players, String[] availableChoices) {
		super(time, name, players, availableChoices);
	}

	public CupidonVote getOtherVote() {
		return otherVote;
	}

	public void setOtherVote(CupidonVote otherVote) {
		this.otherVote = otherVote;
	}

	@Override
	protected void handleResults(Map<GamePlayer, String> results) {
		if (results.size() == 0)
			return;

		String result = results.values().iterator().next();
		if (result == null) {
			finallySelected = "";
		} else {
			finallySelected = result;
		}

		if (otherVote != null && otherVote.finallySelected != null) {
			if (finallySelected.length() == 0 || otherVote.finallySelected.length() == 0) {
				return;
			}

			String otherResult = otherVote.finallySelected;
			GamePlayer first = GamePlayer.getPlayer(finallySelected);
			GamePlayer second = GamePlayer.getPlayer(otherResult);

			first.setLover(second);
			second.setLover(first);

			results.keySet().iterator().next().sendPacket(new MessagePacket(MessageType.GAME, first.getName() + " et " + second.getName() + " sont désormais amoureux !"));
		}
	}

	@Override
	protected boolean allowVote(GamePlayer player, String vote) {
		if (otherVote != null && otherVote.getSelectedValue(player) != null && otherVote.getSelectedValue(player).equalsIgnoreCase(vote)) {
			player.sendPacket(new MessagePacket(MessageType.GAME, "Vous ne pouvez pas sélectionner deux fois la même personne !"));
			return false;
		}
		return true;
	}
}
