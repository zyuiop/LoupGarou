package net.zyuiop.loupgarou.server.game.roledata;

import net.zyuiop.loupgarou.protocol.data.Role;

/**
 * @author zyuiop
 */
public class WitchData extends RoleData {
	private boolean healthPotionUsed = false;
	private boolean killPotionUsed = false;

	public WitchData(Role role) {
		super(role);
	}

	public boolean isHealthPotionUsed() {
		return healthPotionUsed;
	}

	public void setHealthPotionUsed(boolean healthPotionUsed) {
		this.healthPotionUsed = healthPotionUsed;
	}

	public boolean isKillPotionUsed() {
		return killPotionUsed;
	}

	public void setKillPotionUsed(boolean killPotionUsed) {
		this.killPotionUsed = killPotionUsed;
	}
}
