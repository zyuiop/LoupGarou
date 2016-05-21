package net.zyuiop.loupgarou.server.game;

import net.zyuiop.loupgarou.protocol.data.Role;

import java.util.Arrays;

/**
 * @author zyuiop
 */
public class GameConfig {
	private String name;
	private String hoster;
	private final int players;
	private final int villagers;
	private Role[]    characters;
	private String password = null;

	public GameConfig(String name, String hoster, int players, Role... characters) {
		this(name, hoster, players, null, characters);
	}

	public GameConfig(String name, String hoster, int players, String password, Role... characters) {
		this.name = name;
		this.hoster = hoster;
		this.players = players;
		this.characters = characters;
		this.password = password;

		int neededVillagers = players;
		for (Role role : characters)
			if (role == Role.THIEF) {
				neededVillagers ++; // k - 1 + 2 = k + 1
			} else {
				neededVillagers --;
			}

		this.villagers = neededVillagers;
	}

	public String getName() {
		return name;
	}

	public String getHoster() {
		return hoster;
	}

	public int getPlayers() {
		return players;
	}

	public int getVillagers() {
		return villagers;
	}

	public Role[] getCharacters() {
		return characters;
	}

	public Role[] getExtendedCharacters() {
		Role[] array = new Role[characters.length + villagers];
		int i = 0;
		for (Role role : characters)
			array[i++] = role;
		while (i < array.length)
			array[i++] = Role.VILLAGER;

		return array;
	}

	@Override
	public String toString() {
		return "GameConfig{" +
				"name='" + name + '\'' +
				", hoster='" + hoster + '\'' +
				", players=" + players +
				", villagers=" + villagers +
				", characters=" + Arrays.toString(characters) +
				", password='" + password + '\'' +
				'}';
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
