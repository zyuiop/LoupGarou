package net.zyuiop.loupgarou.server.game.roledata;

import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.server.game.GamePlayer;

/**
 * @author zyuiop
 */
public class RoleData {
	private final Role role;
	private GamePlayer modelOf = null;

	public RoleData(Role role) {
		this.role = role;
	}

	public Role getRole() {
		return role;
	}

	public GamePlayer getModelOf() {
		return modelOf;
	}

	public void setModelOf(GamePlayer modelOf) {
		this.modelOf = modelOf;
	}
}
