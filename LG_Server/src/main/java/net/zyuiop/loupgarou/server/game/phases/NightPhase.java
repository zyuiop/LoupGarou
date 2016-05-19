package net.zyuiop.loupgarou.server.game.phases;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.characters.Character;
import net.zyuiop.loupgarou.server.game.characters.Characters;

/**
 * @author zyuiop
 */
public class NightPhase extends GamePhase {
	NightPhase() {
		super(net.zyuiop.loupgarou.game.GamePhase.NIGHT);
	}

	@Override
	protected void invoke(Game game) {
		next(Characters.getCharacter(Role.WOLF, game));
	}
}
