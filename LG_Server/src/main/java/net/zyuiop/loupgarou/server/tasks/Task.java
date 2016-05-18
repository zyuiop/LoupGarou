package net.zyuiop.loupgarou.server.tasks;

import net.zyuiop.loupgarou.server.LGServer;

/**
 * @author zyuiop
 */
public abstract class Task implements Runnable {
	protected Runnable runAfter = null;

	public void complete() {
		if (runAfter != null)
			runAfter.run();
	}

	public void cancel() {
		TaskManager.removeTask(this);
	}

	void tryToRun() {
		run();
		cancel();
	}

	public void setRunAfter(Runnable runAfter) {
		this.runAfter = runAfter;
	}
}
