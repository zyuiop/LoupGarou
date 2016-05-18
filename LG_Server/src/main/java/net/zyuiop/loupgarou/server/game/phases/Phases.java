package net.zyuiop.loupgarou.server.game.phases;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class Phases {
	private static Map<net.zyuiop.loupgarou.game.GamePhase, Class<? extends GamePhase>> phases = new HashMap<>();

	static {
		register(net.zyuiop.loupgarou.game.GamePhase.NIGHT, NightPhase.class);
		register(net.zyuiop.loupgarou.game.GamePhase.DAY, DayPhase.class);
		register(net.zyuiop.loupgarou.game.GamePhase.PREPARATION, PreparationPhase.class);
	}

	private static void register(net.zyuiop.loupgarou.game.GamePhase phase, Class<? extends GamePhase> clazz) {
		phases.put(phase, clazz);
	}

	public static GamePhase getPhase(net.zyuiop.loupgarou.game.GamePhase phase) {
		try {
			return phases.get(phase).getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
}
