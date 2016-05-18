package net.zyuiop.loupgarou.server.tasks;

import net.zyuiop.loupgarou.server.LGServer;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author zyuiop
 */
public class TaskChainer extends Task {
	private ArrayDeque<Task> queue = new ArrayDeque<>();
	private String name;

	public TaskChainer() {
	}

	public TaskChainer(String name) {
		this.name = name;
	}

	public String getName() {
		return name == null ? toString() : name;
	}

	public TaskChainer next(Task task) {
		LGServer.getLogger().info("TaskChainer " + getName() + " : adding " + task + " on top of the queue.");

		task.setRunAfter(this::runNext);
		queue.add(task);
		return this;
	}

	public TaskChainer justAfter(Task task) {
		LGServer.getLogger().info("TaskChainer " + getName() + " : scheduling " + task + " for immediate run");

		task.setRunAfter(this::runNext);
		queue.addFirst(task);
		return this;
	}

	public TaskChainer autoComplete(Runnable runnable) {
		return next(new Task() {
			@Override
			public void run() {
				runnable.run();
				complete();
			}
		});
	}

	public TaskChainer autoCompleteJustAfter(Runnable runnable) {
		return justAfter(new Task() {
			@Override
			public void run() {
				runnable.run();
				complete();
			}
		});
	}

	private void runNext() {
		Task task = queue.poll();
		LGServer.getLogger().info("TaskChainer " + getName() + " : running " + task + " next.");
		if (task != null)
			task.run();
		else
			complete();
	}

	@Override
	public void run() {
		runNext();
	}
}
