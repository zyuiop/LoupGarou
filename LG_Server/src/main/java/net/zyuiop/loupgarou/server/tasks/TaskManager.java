package net.zyuiop.loupgarou.server.tasks;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author zyuiop
 */
public class TaskManager {
	private static List<Task>               tasks    = new ArrayList<>();
	private static boolean                  running  = true;
	private static ListeningExecutorService executor = setupWorkers();

	private static ListeningExecutorService setupWorkers() {
		int processor = Runtime.getRuntime().availableProcessors();
		ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("Scheduler - #%d").build();
		ExecutorService service = Executors.newFixedThreadPool(processor, factory);
		return MoreExecutors.listeningDecorator(service);
	}

	public static void stop() {
		running = false;
	}

	public static void removeTask(Task task) {
		tasks.remove(task);
	}

	public static void submit(Task task) {
		tasks.add(task);
	}

	public static void runAsync(Runnable runnable) {
		executor.submit(runnable);
	}

	static {
		new Thread(() -> {
			while (running) {
				long start = System.currentTimeMillis();
				List<Task> tasks = Lists.newArrayList(TaskManager.tasks);
				tasks.forEach(Task::tryToRun);

				try {
					Thread.sleep((start - System.currentTimeMillis()) + 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "SchedulerThread").start();
	}
}
