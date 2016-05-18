package net.zyuiop.loupgarou.server.game.roledata;

import net.zyuiop.loupgarou.game.Role;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class RoleDatas {
	private static Map<Role, Class<? extends RoleData>> roleClassMap = new HashMap<>();

	static {
		roleClassMap.put(Role.WITCH, WitchData.class);
	}

	public static RoleData create(Role role) {
		try {
			if (!roleClassMap.containsKey(role))
				return new RoleData(role);
			return roleClassMap.get(role).getConstructor(Role.class).newInstance(role);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return new RoleData(role);
	}
}
