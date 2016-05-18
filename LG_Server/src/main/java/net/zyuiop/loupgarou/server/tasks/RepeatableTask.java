package net.zyuiop.loupgarou.server.tasks;

/**
 * @author zyuiop
 */
public abstract class RepeatableTask extends Task {
	private int interval = 1;
	private int delay = 0;

	public RepeatableTask(int interval, int delay) {
		this.interval = interval;
		this.delay = delay;
	}

	void tryToRun() {
		delay--;
		if (delay == 0) {
			delay = interval;
			run();
		}
	}
}
