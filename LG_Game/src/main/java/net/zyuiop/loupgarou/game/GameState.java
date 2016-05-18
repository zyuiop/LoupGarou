package net.zyuiop.loupgarou.game;

/**
 * @author zyuiop
 */
public enum GameState {
	WAITING("En attente"),
	PREPARING("Préparation"),
	STARTED("Démarré"),
	FINISHED("Terminé");

	private final String humanState;

	GameState(String humanState) {
		this.humanState = humanState;
	}

	public String getHumanState() {
		return humanState;
	}
}
