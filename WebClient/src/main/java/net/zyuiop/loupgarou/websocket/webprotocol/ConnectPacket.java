package net.zyuiop.loupgarou.websocket.webprotocol;

/**
 * Created by zyuiop on 30/07/2016.
 * Part of the lg-parent project.
 */
public class ConnectPacket {
	private String ip;
	private Integer port;
	private String name;

	public ConnectPacket() {
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
