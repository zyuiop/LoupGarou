package net.zyuiop.loupgarou.server.game.phases;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.characters.Characters;

/**
 * @author zyuiop
 */
public class EndNightPhase extends GamePhase {
	EndNightPhase() {
		super(net.zyuiop.loupgarou.game.GamePhase.END_NIGHT);
	}

	@Override
	protected void invoke(Game game) {
		next(Characters.getCharacter(Role.WITCH, game));
	}
}
