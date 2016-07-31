roles = {
    "VILLAGER" : {display: "Simple Villageois", power: "Aucun", objective: "Éliminer tous les Loup Garous."},
    "WOLF" : {display: "Loup Garou", power: "Tue un villageois chaque nuit", objective: "Éliminer tous les villageois"},
    "MEDIUM" : {display: "Voyante", power: "Peut observer la carte d'un joueur chaque nuit", objective: "Éliminer tous les Loup Garous"},
    "HUNTER" : {display: "Chasseur", power: "Lors de sa mort, il peut choisir d'éliminer un autre joueur.", objective: "Éliminer tous les Loup Garous"},
    "CUPIDON" : {display: "Cupidon", power: "Désigne deux amoureux : si l'un meurt l'autre se suicide", objective: "Éliminer tous les Loup Garous"},
    "WITCH" : {display: "Sorcière", power: "Dispose d'une potion de vie et d'une potion de mort", objective: "Éliminer tous les Loup Garous"},
    "LITTLE_GIRL" : {display: "Petite Fille", power: "Peut espionner les loups durant la nuit", objective: "Éliminer tous les Loup Garous"},
    "THIEF" : {display: "Voleur", power: "Peut choisir une carte parmi deux en début de partie", objective: "Dépend de la carte choisie"},
    "SAVER" : {display: "Salvateur", power: "Peut protéger une personne des Loups Garous", objective: "Éliminer tous les Loup Garous"},
    "WHITE_WOLF" : {display: "Loup Garou Blanc", power: "Peut tuer un loup garou une nuit sur deux", objective: "Être le dernier en vie"},
    "ANCIENT" : {display: "Ancien", power: "Résiste à un assaut des loups, fait perdre leur pouvoir aux villageois s'ils le tuent", objective: "Éliminer tous les Loup Garous"},
    "ANGEL" : {display: "Ange", power: "Si le village le tue au premier tour, il gagne la partie.", objective: "Être tué par le village au premier tour"},
    "IDIOT" : {display: "Idiot du village", power: "Ne meurt pas si le village le tue mais ne peut plus voter", objective: "Éliminer tous les Loup Garous"},
    "GREAT_BAD_WOLF" : {display: "Grand Méchant Loup", power: "Tant qu'aucun loup n'est port, désigne une seconde victime chaque nuit", objective: "Éliminer tous les villageois"},
    "SAVAGE_KID" : {display: "Enfant Sauvage", power: "Se choisit un idole. Si celui ci meurt, il se transformera en loup garou", objective: "Éliminer tous les Loup Garous"}
};

gameState = {
    "WAITING" : "En attente",
    "PREPARING" : "Préparation",
    "STARTED" : "Démarré",
    "FINISHED" : "Terminé"
};

gamePhase = {
    "DAY" : "Jour",			// Vote
    "PREPARATION" : "Préparation",	// Cupidon, Stealer...
    "PRE_NIGHT" : "Début de la nuit",		// Voyante, Salvateur...
    "NIGHT" : "Nuit",			// Loups Garous
    "END_NIGHT" : "Fin de la nuit"	// Loups Blancs, Sorcière, Corbeau...
};