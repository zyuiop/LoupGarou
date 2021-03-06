package net.zyuiop.loupgarou.server.game.characters;

import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.server.game.Game;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class Characters {
	private static Map<Role, Class<? extends Character>> characters = new HashMap<>();

	static {
		characters.put(Role.WOLF, WolfCharacter.class);
		characters.put(Role.THIEF, ThiefCharacter.class);
		characters.put(Role.CUPIDON, CupidonCharacter.class);
		characters.put(Role.WITCH, WitchCharacter.class);
		characters.put(Role.MEDIUM, MediumCharacter.class);
		characters.put(Role.SAVER, SaverCharacter.class);
		characters.put(Role.WHITE_WOLF, WhiteWolfCharacter.class);
		characters.put(Role.GREAT_BAD_WOLF, BadWolfCharacter.class);
		characters.put(Role.SAVAGE_KID, SavageKidCharacter.class);
	}

	public static Character getCharacter(Role role, Game game) {
		try {
			return characters.get(role).getDeclaredConstructor(Game.class).newInstance(game);
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
}
