package net.zyuiop.loupgarou.protocol.data;

/**
 * @author zyuiop
 */
public enum Role {
	VILLAGER("Simple Villageois", "Aucun", "Éliminer tous les Loup Garous."),
	WOLF("Loup Garou", "Tue un villageois chaque nuit", "Éliminer tous les villageois"),
	MEDIUM("Voyante", "Peut observer la carte d'un joueur chaque nuit", "Éliminer tous les Loup Garous"),
	HUNTER("Chasseur", "Lors de sa mort, il peut choisir d'éliminer un autre joueur.", "Éliminer tous les Loup Garous"),
	CUPIDON("Cupidon", "Désigne deux amoureux : si l'un meurt l'autre se suicide", "Éliminer tous les Loup Garous"),
	WITCH("Sorcière", "Dispose d'une potion de vie et d'une potion de mort", "Éliminer tous les Loup Garous"),
	LITTLE_GIRL("Petite Fille", "Peut espionner les loups durant la nuit", "Éliminer tous les Loup Garous"),
	THIEF("Voleur", "Peut choisir une carte parmi deux en début de partie", "Dépend de la carte choisie"),
	SAVER("Salvateur", "Peut protéger une personne des Loups Garous", "Éliminer tous les Loup Garous"),
	WHITE_WOLF("Loup Garou Blanc", "Peut tuer un loup garou une nuit sur deux", "Être le dernier en vie"),
	ANCIENT("Ancien", "Résiste à un assaut des loups, fait perdre leur pouvoir aux villageois s'ils le tuent", "Éliminer tous les Loup Garous"),
	ANGEL("Ange", "Si le village le tue au premier tour, il gagne la partie.", "Être tué par le village au premier tour"),

	IDIOT("Idiot du village", "Ne meurt pas si le village le tue mais ne peut plus voter", "Éliminer tous les Loup Garous"),
	GREAT_BAD_WOLF("Grand Méchant Loup", "Tant qu'aucun loup n'est port, désigne une seconde victime chaque nuit", "Éliminer tous les villageois"),
	SAVAGE_KID("Enfant Sauvage", "Se choisit un idole. Si celui ci meurt, il se transformera en loup garou", "Éliminer tous les Loup Garous");

	private final String name;
	private final String power;
	private final String objective;

	Role(String name, String power, String objective) {
		this.name = name;
		this.power = power;
		this.objective = objective;
	}

	Role() {
		this("Not defined", "Not defined", "Not defined");
	}

	public String getName() {
		return name;
	}

	public String getPower() {
		return power;
	}

	public String getObjective() {
		return objective;
	}
}
