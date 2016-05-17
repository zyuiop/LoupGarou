package net.zyuiop.loupgarou.server.game;

import net.zyuiop.loupgarou.game.Role;

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

	public GameConfig(String name, String hoster, int players, Role... characters) {
		this.name = name;
		this.hoster = hoster;
		this.players = players;
		this.characters = characters;

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

	@Override
	public String toString() {
		return "GameConfig{" +
				"name='" + name + '\'' +
				", hoster='" + hoster + '\'' +
				", players=" + players +
				", villagers=" + villagers +
				", characters=" + Arrays.toString(characters) +
				'}';
	}
}
