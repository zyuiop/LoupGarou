package net.zyuiop.loupgarou.server.game.phases;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class Phases {
	private static Map<net.zyuiop.loupgarou.protocol.data.GamePhase, Class<? extends GamePhase>> phases = new HashMap<>();

	static {
		register(net.zyuiop.loupgarou.protocol.data.GamePhase.PRE_NIGHT, PreNightPhaase.class);
		register(net.zyuiop.loupgarou.protocol.data.GamePhase.NIGHT, NightPhase.class);
		register(net.zyuiop.loupgarou.protocol.data.GamePhase.END_NIGHT, EndNightPhase.class);
		register(net.zyuiop.loupgarou.protocol.data.GamePhase.DAY, DayPhase.class);
		register(net.zyuiop.loupgarou.protocol.data.GamePhase.PREPARATION, PreparationPhase.class);
	}

	private static void register(net.zyuiop.loupgarou.protocol.data.GamePhase phase, Class<? extends GamePhase> clazz) {
		phases.put(phase, clazz);
	}

	public static GamePhase getPhase(net.zyuiop.loupgarou.protocol.data.GamePhase phase) {
		try {
			return phases.get(phase).getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
}
