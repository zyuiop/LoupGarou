package net.zyuiop.loupgarou.server.game.roledata;

import net.zyuiop.loupgarou.protocol.data.Role;

/**
 * @author zyuiop
 */
public class SaverData extends RoleData {
	private String lastSaved;

	public SaverData(Role role) {
		super(role);
	}

	public String getLastSaved() {
		return lastSaved;
	}

	public void setLastSaved(String lastSaved) {
		this.lastSaved = lastSaved;
	}
}
