package net.zyuiop.loupgarou.server.game.characters;

import net.zyuiop.loupgarou.protocol.threading.Task;
import net.zyuiop.loupgarou.server.game.Game;

/**
 * @author zyuiop
 */
public abstract class Character extends Task {
	protected final Game game;

	protected Character(Game game) {
		this.game = game;
	}
}
