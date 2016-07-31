var handler = {};
var games = {};
var currentGame = null;

handler.socket = null;
handler.sendPacket = function (packet) {
    this.socket.send(JSON.stringify(packet))
};
handler.handlers = {};

handler.handlers[1] = function (packet) {
    findGameWindow();
    alert("Partie quitté : " + packet.reason);
};

handler.handlers[2] = function (packet) {
    games = {};

    for (game of packet.games)
        games[game.id] = game;

    gameRefreshHandler();
};

handler.handlers[3] = function (packet) {
    currentGame = {
        id: packet.id,
        name: packet.name,
        host: packet.hoster
    };
    gameWindow();
};

handler.handlers[4] = function (packet) {
    // LOGIN RESPONSE
    if (packet.success) {
        console.log("Connected ! " + packet.errorMessage);
        findGameWindow();
    } else {
        alert("Connexion failed : " + packet.errorMessage);

    }
};

handler.handlers[5] = function (packet) {
    // SEND MESSAGE
    if (packet.type === "ERROR")
        alert("Erreur : " + packet.message)
    else
        appendToChat(packet);
};

handler.handlers[6] = function (packet) {
    currentGame.phase = packet.phase;
    refreshGameState();
};

handler.handlers[7] = function (packet) {
    currentGame.role = packet.role;
    refreshGameState();
};

handler.handlers[8] = function (packet) {
    currentGame.state = packet.state;
    refreshGameState();
};

handler.handlers[9] = function (packet) {
    currentGame.maxPlayers = packet.maxPlayers;
    currentGame.players = packet.players;
    refreshPlayers();
};


handler.handlers[0x0A] = function (packet) {
    console.log("Vote end " + packet.voteId);

    var voteId = packet.voteId;
    $("#vote-" + voteId).remove();
    $("#players-vote-" + voteId).remove();
};

handler.handlers[0x0B] = function (packet) {
    generateVote(packet);
};

handler.handlers[0x0C] = function (packet) {
    console.log("Vote value : " + packet.voteId + " / " + packet.votingPlayer + " votes " + packet.vote);
    // VOTE VALUE
    $("#player-vote-" + packet.voteId + "-" + packet.votingPlayer).text(packet.vote);
};

handler.handlers[0x0D] = function (packet) {
    currentGame.composition = packet.roles;
    refreshRoles();
};

resetGameWindow = function () {
    $("#game-state-role").hide();
    $("#game-state-turn").hide();
    $("#role-frame").hide();
    $("#players").html("");
    $("#composition-frame").hide();
    $("#chat-content").html("");
    $("#messageField").val("");

    $("#votes").html("");
    $("#votes-values").html("");
};

generateVote = function(packet) {
    var voteId = packet.voteId;
    var form = "<form id='vote-form-" + voteId + "'><input type='hidden' name='voteid' value='" + voteId + "' />";
    form += "<div class='form-group'><select name='votevalue' id='vote-form-value-'" + voteId + "' class='form-control'>";

    for (var choice of packet.availableChoices) {
        form += "<option value='" + choice + "'>" + choice + "</option>";
    }
    form += "</select></div>";
    form += "<button type='submit' class='btn btn-default' style='width: 100%;'>Voter</button>";

    var voteBlock = "<div id='vote-" + voteId + "' class='panel panel-primary'>";
    voteBlock += '<div class="panel-heading"> <h3 class="panel-title">' + packet.chooseReason + ' <small id="vote-' + voteId + '-time">' +
        '(' + packet.chooseTime + ')</small></h3> </div>';
    voteBlock += '<div class="panel-body">' + form + '</div></div>';

    $("#votes").append(voteBlock);


    var voteValuesBlock = '<div id="players-vote-' + voteId + '" class="panel panel-primary">';
    voteValuesBlock += "<div class='panel-heading'><h3 class='panel-title'>" + packet.chooseReason + "</h3></div>";
    voteValuesBlock += "<div class='panel-body'><p>Votes des joueurs :</p><ul>";

    for (var voter of packet.voters) {
        voteValuesBlock += "<li>" + voter + " : <span id='player-vote-" + voteId + "-" + voter + "'>Aucun vote</span></li>";
    }

    voteValuesBlock += "</ul></div></div>";

    $("#votes-values").append(voteValuesBlock);


    time = packet.chooseTime;
    refresh = function() {
        time --;
        $("#vote-" + voteId + "-time").text("(" + time + ")");

        if (time > 0) {
            setTimeout(refresh, 1000);
        }
    };

    setTimeout(refresh, 1000);

    $("#vote-form-" + voteId).on("submit", function (e) {
        e.preventDefault();
        var value = $("#vote-form-value-" + voteId).val();

        handler.sendPacket({
            voteId: voteId,
            vote: value,
            packetId: 0xA3
        })
    })
};

refreshGameState = function () {
    $("#game-state-name").text("Nom : " + currentGame.name);
    $("#game-state-host").text("Host : " + currentGame.host);

    if (typeof currentGame.state !== "undefined") {
        $("#game-state-state").text("Êtat : " + gameState[currentGame.state]);
    }

    if (typeof currentGame.role !== "undefined") {
        $("#game-state-role").text("Personnage : " + roles[currentGame.role].display);
        $("#game-state-role").show();
        $("#role").html("<img src='card-" + currentGame.role + ".jpg' />");
        $("#role-frame").show();
    }

    if (typeof currentGame.turn !== "undefined") {
        $("#game-state-turn").text("Tour : " + gamePhase[currentGame.turn]);
        $("#game-state-turn").show();
    }
};

refreshPlayers = function () {
    var html = "<h3>Joueurs (" + currentGame.players.length + " / " + currentGame.maxPlayers + ")</h3>";
    html += "<ul>";

    for (var player of currentGame.players)
        html += "<li>" + player + "</li>";

    html += "</ul>";
    $("#players").html(html);
};

refreshRoles = function () {
    var roleMap = {};

    for (var roleId in currentGame.composition) {
        var role = currentGame.composition[roleId];
        if (role in roleMap)
            roleMap[role] += 1;
        else
            roleMap[role] = 1;
    }

    var html = "<ul>";
    for (var role in roleMap) {
        if (role in roles) {
            html += "<li>" + roles[role].display + (roleMap[role] > 1 ? " (" + roleMap[role] + ")" : "") + "</li>";
        } else {
            console.log("Unknown role found : " + role);
        }
    }

    html += "</ul>";
    $("#composition").html(html);
    $("#composition-frame").show();
};

appendToChat = function(packet) {
    var tag = "<span syle='"

    if (packet.red >= 0 && packet.green >= -1 && packet.blue >= -1)
        tag += "color: rgb(" + packet.red + ", " + packet.green + ", " + packet.blue + ");";

    tag += "'>";

    if (packet.type === "USER")
        tag += "[" + packet.sender + "]";
    else if (packet.type === "GAME")
        tag += "(Jeu)";
    else
        tag += "(Système)";

    tag += " " + packet.message;
    $("#chat-content").append("<p>" + tag + "</span></p>");

    var chatContent = document.getElementById("chat-content");
    chatContent.scrollTop = chatContent.scrollHeight;
};

gameRefreshHandler = function () {
    var html = "";

    for (var gameId in games) {
        var game = games[gameId];
        html += "<tr>";
        html += "<td><a onclick='join(" + gameId + ")' href='#'>" + game.gameName + "</a></td>";
        html += "<td>" + game.hoster + "</td>";
        html += "<td>" + game.currentPlayers + " / " + game.maxPlayers + "</td>";
        html += "<td>" + gameState[game.state] + "</td>";
        html += "</td>";
    }

    if (html === "") {
        html = "<tr> <td colspan=\"4\">Aucun contenu pour le moment.</td> </tr>";
    }

    $("#table-content").html(html);
};

$(document).ready(function () {
    $('#creation-form').on('submit', function (e) {
        e.preventDefault();
        create();
    });

    $("#refresh-btn").on('click', function (e) {
        doRefresh();
    });

    $('#connectForm').on('submit', function (e) {
        e.preventDefault();
        connect();
    });

    $("#form-chat").on("submit", function(e) {
        e.preventDefault();
        var msg = $("#messageField").val().trim();
        if (msg.length >= 0) {
            handler.sendPacket({
                packetId: 0xA5,
                message: msg
            });
        }
        $("#messageField").val("");
    });

    $("#btn-quit").on("click", function (e) {
        handler.sendPacket({
            packetId: 0xA2,
            gameId: -1
        });

        $("#btn-quit").button("loading");
    });

    connectWindow();
});

/**
 * Used to join a game
 * @param gameId id of the game to join
 */
function join(gameId) {
    console.log("Joining game " + gameId);
    var game = games[gameId];
    if (typeof game !== "undefined") {
        var packet = {
            gameId: gameId,
            packetId: 0xA2
        };

        if (game.hasPassword) {
            packet.password = prompt("Mot de passe de la partie");
        }

        handler.sendPacket(packet);
    }
}

/**
 * Used to connect to the server and open the socket
 * @param name Name of the player
 * @param ip IP of the server
 * @param port port of the server
 */
function doConnect(name, ip, port) {
    if (handler.socket !== null) {
        handler.socket.close()
    }

    handler.socket = new WebSocket("ws://localhost:8000/game");
    handler.socket.onopen = function () {
        handler.sendPacket({
            name: name,
            ip: ip,
            port: port
        });
    };

    handler.socket.onmessage = function (message) {
        var msg = JSON.parse(message.data);
        console.log(msg);
        var packetId = msg.packetId;
        var handleMethod = handler.handlers[packetId];
        if (typeof handleMethod !== "undefined") {
            handleMethod(msg);
        } else
            console.log("No handler for packet " + packetId + " :(")
    };
}

/**
 * Creates a game using the content of the creation form.
 */
function create() {
    var name = $("#gameName").val();
    var villagers = parseInt($("#villagers").val(), 10);
    var wolves = parseInt($("#wolves").val(), 10);

    if (villagers < 0) {
        // TODO : Cleaner stuff
        alert("Merci d'indiquer un nombre de villageois positif ou nul.");
        return;
    }

    if (wolves < 1) {
        alert("Il doit y avoir au moins un loup !");
        return;
    }

    var roles = $("#characters").val();
    var maxPl = villagers + wolves + roles.length;
    if ($.inArray("THIEF", roles) >= 0)
        maxPl -= 2;

    console.log("Max Players : " + maxPl + ", composed of " + villagers + " villagers, " + wolves + " wolves and " + roles.length + " additional roles. Thief ? " + $.inArray("THIEF", roles))

    if (maxPl > 70) {
        alert("Il est impossible d'accepter plus de 70 joueurs dans une même partie.");
        return;
    }

    if (maxPl < 2) {
        alert("Il faut minimum deux joueurs dans une partie.");
        return;
    }

    var i = 0;
    while (i < wolves) {
        roles.push("WOLF");
        i += 1;
    }

    var packet = {
        packetId: 0xA1,
        name: name,
        players: maxPl,
        characters: roles
    };

    var password = $("#password").val();
    if (password != "") {
        packet.password = sha256(password);
    }

    handler.sendPacket(packet);
}

/**
 * Requests gamelist refresh
 */
function doRefresh() {
    handler.sendPacket({
        packetId: 0xA4
    });
}


function connect() {
    var name = $("#nickname").val();
    var ip = $("#ip").val();
    var port = $("#port").val();

    doConnect(name, ip, port);
}

function findGameWindow() {
    currentGame = null;

    $("#connect-window").hide();
    $("#findgame-window").show();
    $("#game-window").hide();

    gameRefreshHandler();
}

function gameWindow() {
    $("#connect-window").hide();
    $("#findgame-window").hide();
    $("#game-window").show();

    resetGameWindow();
    refreshGameState();
    refreshPlayers();
    refreshRoles();
}

function connectWindow() {
    $("#connect-window").show();
    $("#findgame-window").hide();
    $("#game-window").hide();
}
