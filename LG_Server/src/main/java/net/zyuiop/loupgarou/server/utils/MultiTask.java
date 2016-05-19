package net.zyuiop.loupgarou.server.utils;

import com.google.common.collect.Lists;
import net.zyuiop.loupgarou.game.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author zyuiop
 */
public class MultiTask extends Task {
	private final Collection<Task> tasks;

	public MultiTask(Collection<Task> tasks) {
		this.tasks = tasks;
	}

	public MultiTask(Task... tasks) {
		this(Lists.newCopyOnWriteArrayList(Lists.newArrayList(tasks)));
	}

	@Override
	public void run() {
		tasks.forEach(task -> {
			task.setRunAfter(() -> finish(task));
			task.run();
		});
	}

	private void finish(Task task) {
		tasks.remove(task);
		if (tasks.size() == 0)
			complete();
	}
}
