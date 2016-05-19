package net.zyuiop.loupgarou.client.data;

import net.zyuiop.loupgarou.client.LGClient;

public class SavedServer {
	private String ip;
	private int    port;
	private String username;

	public SavedServer() {
	}

	public SavedServer(String ip, int port, String username) {
		this.ip = ip;
		this.port = port;
		this.username = username;
	}

	public void connect(LGClient client) {
		client.connect(ip, port, username);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}