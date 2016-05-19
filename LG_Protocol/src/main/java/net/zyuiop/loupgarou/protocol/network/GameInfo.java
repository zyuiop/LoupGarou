package net.zyuiop.loupgarou.protocol.network;

import net.zyuiop.loupgarou.game.GameState;

/**
 * @author zyuiop
 */
public class GameInfo {
	private int id;
	private String    gameName;
	private String    hoster;
	private GameState state;
	private int       currentPlayers;
	private int       maxPlayers;
	private boolean hasPassword;

	public GameInfo(int id, String gameName, String hoster, GameState state, int currentPlayers, int maxPlayers, boolean hasPassword) {
		this.id = id;
		this.gameName = gameName;
		this.hoster = hoster;
		this.state = state;
		this.currentPlayers = currentPlayers;
		this.maxPlayers = maxPlayers;
		this.hasPassword = hasPassword;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public String getHoster() {
		return hoster;
	}

	public void setHoster(String hoster) {
		this.hoster = hoster;
	}

	public GameState getState() {
		return state;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public int getCurrentPlayers() {
		return currentPlayers;
	}

	public void setCurrentPlayers(int currentPlayers) {
		this.currentPlayers = currentPlayers;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public boolean isHasPassword() {
		return hasPassword;
	}

	public void setHasPassword(boolean hasPassword) {
		this.hasPassword = hasPassword;
	}

	@Override
	public String toString() {
		return "GameInfo{" +
				"id=" + id +
				", gameName='" + gameName + '\'' +
				", hoster='" + hoster + '\'' +
				", state=" + state +
				", currentPlayers=" + currentPlayers +
				", maxPlayers=" + maxPlayers +
				", hasPassword=" + hasPassword +
				'}';
	}
}
