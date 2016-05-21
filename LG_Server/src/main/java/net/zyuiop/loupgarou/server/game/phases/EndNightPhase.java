package net.zyuiop.loupgarou.server.game.phases;

import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.characters.Characters;
import net.zyuiop.loupgarou.server.utils.MultiTask;

/**
 * @author zyuiop
 */
public class EndNightPhase extends GamePhase {
	EndNightPhase() {
		super(net.zyuiop.loupgarou.protocol.data.GamePhase.END_NIGHT);
	}

	@Override
	protected void invoke(Game game) {
		next(new MultiTask(Characters.getCharacter(Role.WHITE_WOLF, game), Characters.getCharacter(Role.WITCH, game)));
	}
}
