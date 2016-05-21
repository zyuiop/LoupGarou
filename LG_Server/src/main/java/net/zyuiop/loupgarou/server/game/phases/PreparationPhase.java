package net.zyuiop.loupgarou.server.game.phases;

import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.characters.Characters;
import net.zyuiop.loupgarou.server.game.characters.ThiefCharacter;
import net.zyuiop.loupgarou.server.utils.MultiTask;

/**
 * @author zyuiop
 */
public class PreparationPhase extends GamePhase {
	PreparationPhase() {
		super(net.zyuiop.loupgarou.protocol.data.GamePhase.PREPARATION);
	}

	private Role[] roles = null;

	public void setRoles(Role[] roles) {
		this.roles = roles;
	}

	@Override
	protected void invoke(Game game) {
		ThiefCharacter thiefCharacter = (ThiefCharacter) Characters.getCharacter(Role.THIEF, game);
		thiefCharacter.setRoles(roles);
		next(thiefCharacter);
		next(new MultiTask(Characters.getCharacter(Role.CUPIDON, game), Characters.getCharacter(Role.SAVAGE_KID, game)));
	}
}
