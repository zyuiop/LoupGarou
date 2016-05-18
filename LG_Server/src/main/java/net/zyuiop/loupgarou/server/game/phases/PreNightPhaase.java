package net.zyuiop.loupgarou.server.game.phases;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.characters.Characters;

/**
 * @author zyuiop
 */
public class PreNightPhaase extends GamePhase {
	PreNightPhaase() {
		super(net.zyuiop.loupgarou.game.GamePhase.PRE_NIGHT);
	}

	@Override
	protected void invoke(Game game) {
		next(Characters.getCharacter(Role.MEDIUM, game));
		next(Characters.getCharacter(Role.SAVER, game));
	}
}
