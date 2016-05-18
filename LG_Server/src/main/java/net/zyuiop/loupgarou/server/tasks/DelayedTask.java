package net.zyuiop.loupgarou.server.tasks;

/**
 * @author zyuiop
 */
public abstract class DelayedTask extends Task {
	private int delay = 0;

	public DelayedTask(int delay) {
		this.delay = delay;
	}

	void tryToRun() {
		delay--;
		if (delay == 0) {
			run();
			cancel();
		}
	}
}
