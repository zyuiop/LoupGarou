package net.zyuiop.loupgarou.protocol.data;

/**
 * @author zyuiop
 */
public enum GamePhase {
	DAY("Jour"),			// Vote
	PREPARATION("Préparation"),	// Cupidon, Stealer...
	PRE_NIGHT("Début de la nuit"),		// Voyante, Salvateur...
	NIGHT("Nuit"),			// Loups Garous
	END_NIGHT("Fin de la nuit")		// Loups Blancs, Sorcière, Corbeau...
	;

	private final String humanPhase;

	GamePhase(String humanPhase) {
		this.humanPhase = humanPhase;
	}

	public String getHumanPhase() {
		return humanPhase;
	}
}
