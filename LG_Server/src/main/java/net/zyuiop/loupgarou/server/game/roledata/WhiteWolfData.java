package net.zyuiop.loupgarou.server.game.roledata;

import net.zyuiop.loupgarou.protocol.data.Role;

/**
 * @author zyuiop
 */
public class WhiteWolfData extends RoleData {
	private boolean ateLastTurn = true; // premier tour de bouffe : 2nd tour

	public WhiteWolfData(Role role) {
		super(role);
	}

	public boolean passTurn() {
		ateLastTurn = !ateLastTurn;
		return ateLastTurn;
	}
}
