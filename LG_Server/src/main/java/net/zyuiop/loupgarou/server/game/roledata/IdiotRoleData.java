package net.zyuiop.loupgarou.server.game.roledata;

import net.zyuiop.loupgarou.protocol.data.Role;

/**
 * @author zyuiop
 */
public class IdiotRoleData extends RoleData {
	private boolean isDead = false;

	public IdiotRoleData(Role role) {
		super(role);
	}

	public void villageKill() {
		isDead = true;
	}

	public boolean isDead() {
		return isDead;
	}
}
