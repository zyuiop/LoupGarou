package net.zyuiop.loupgarou.server.game.phases;

import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.utils.TaskChainer;

/**
 * @author zyuiop
 */
public abstract class GamePhase extends TaskChainer {
	private final net.zyuiop.loupgarou.protocol.data.GamePhase phase;

	protected GamePhase(net.zyuiop.loupgarou.protocol.data.GamePhase phase) {
		super(phase.name() + "-Chainer");
		this.phase = phase;
	}

	public net.zyuiop.loupgarou.protocol.data.GamePhase getPhase() {
		return phase;
	}

	public void run(Game game) {
		game.setPhase(phase);
		invoke(game);
		run();
	}

	protected abstract void invoke(Game game);
}
