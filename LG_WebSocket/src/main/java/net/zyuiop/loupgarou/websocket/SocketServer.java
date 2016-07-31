package net.zyuiop.loupgarou.websocket;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import javax.websocket.DeploymentException;
import org.glassfish.tyrus.server.Server;

/**
 * Created by zyuiop on 30/07/2016.
 * Part of the lg-parent project.
 */
public class SocketServer {
	public static Logger logger = Logger.getGlobal();
	public static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

	public static void main(String[] args) throws DeploymentException {
		Server server = new Server("localhost", 8000, "/", SocketEndpoint.class);
		server.start();
		logger.info("Server started.");

		new Scanner(System.in).nextLine();

		server.stop();
		logger.info("Server stopped.");
	}

	public static ScheduledExecutorService getScheduler() {
		return service;
	}
}
