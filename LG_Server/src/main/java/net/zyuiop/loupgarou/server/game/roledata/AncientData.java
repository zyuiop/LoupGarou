package net.zyuiop.loupgarou.server.game.roledata;

import net.zyuiop.loupgarou.game.Role;

/**
 * @author zyuiop
 */
public class AncientData extends RoleData {
	private boolean wasFirstKilled = false;

	public AncientData(Role role) {
		super(role);
	}

	public boolean tryToKill() {
		if (wasFirstKilled)
			return true;
		wasFirstKilled = true;
		return false;
	}
}
