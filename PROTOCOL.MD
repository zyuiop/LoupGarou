Protocol Documentation
======================

Current protocol version : 6

## Some types definitions :

### Usual types

| type name | size    | other information
|-----------|---------|---------------------
| byte      | 1 byte  | unsigned byte, max value 255
| int       | 4 bytes | signed integer
| short     | 2 bytes | signed integer

### String :

| type    | description                            
|---------|----------------------------------------
| int (4) | size of the following array (or 0 for null)
| byte[]  | array of bytes representing the string  (if size > 0)

### Array

They are marked as `T[]` where T is the type of the array.

| type        | name         | description                                                                   |
|-------------|--------------|-------------------------------------------------------------------------------|
| boolean     | great amount | if the size is greater than 255, this boolean has to be true                  |
| int or byte | size         | size of the array. If `great amount` is true, it's an int, else it's a byte.  |
| T           | values       | the values of the array, type depends on the array type                       |

### GameInfo :

A GameInfo object describes a game

| type      | name           | description
|-----------|----------------|--------------
| int       | id             | id of the game
| string    | gameName       | the name of the game
| string    | hoster         | the name of the player who hosts the game
| GameState | state          | the current gamestate of this game
| byte      | currentPlayers | The current amount of players
| byte      | maxPlayers     | the maximal amount of players in this game
| boolean   | hasPassword    | whether the game has or not a password

### Enumerations

The `index` value is used in the packet. It is always encoded as a byte.

#### GamePhase

| index | description
|-------|--------------
| 0     | Day
| 1     | Preparation
| 2     | Beggining of the night
| 3     | Middle of the night
| 4     | End of the night

#### Game State

| index | description
|-------|--------------
| 0     | Waiting : The game hasn't started yet 
| 1     | Preparing : The game cannot be joined anymore but hasn't started yet
| 2     | Started : The game is in progress
| 3     | Finished : The game is over and will be removed soon

#### Message Type

| index | description
|-------|--------------
| 0     | User : game chat messages
| 1     | System : system related messages (i.e: game joined...)
| 2     | Game : game related messages (i.e: kills, role attributions...)
| 3     | Error : error messages, displayed in an Alert modal in the original client

#### MessageModifier

| index | description
|-------|--------------
| 0     | Normal : message is show normally
| 1     | Bold : message is displayed in bold
| 2     | Italic : message is displayed in italic
| 3     | Bold and italic : message is displayed in bold and italic

#### Role

| index | name
|-------|------------
| 0     | Villager
| 1     | Wolf
| 2     | Medium *(Voyante)*
| 4     | Hunter
| 5     | Cupidon
| 6     | Witch
| 7     | Little girl
| 8     | Thief
| 9     | Saver *(Salvateur)*
| 10    | White wolf
| 11    | Elder
| 12    | Angel


## Packet structure

| type    | description        |
|---------|--------------------|
| int     | size of the packet |
| byte    | id of the packet   |
| ...     | packet content     |

## Server -> Client

### 0x01 : Game leave

This packet is sent when the user leaves his game.

| type   | name   | description
|--------|--------|-------------
| string | reason | The reason the user left the game (kick, game removed, requested...)

### 0x02 : Game list

This packet is sent when the user joins a server if he isn't in a game, and on request (0xA4 : refresh game list). It contains data about all the games in progress.

| type       | name  | description
|------------|-------|-------------
| GameInfo[] | games | Array of all the available games

### 0x03 : Game join confirm

Sent by the server when the player joins a game, whether because he requested it (by sending 0xA1 or 0xA2) or because he reconnected on a server on which he was playing a game.

| type   | name | description
|--------|------|-------------
| int    | id   | id of the joined game
| string | name | name of the joined game
| string | host | name of the player who hosts the game

### 0x04 : Login response

Sent by the server as a response to 0x00

| type    | name    | description
|---------|---------|-------------
| boolean | success | true if login succeeded, false an error occured (the connexion will be closed just after sending this packet)
| string  | message | the error message or a welcome message if login succeeded

### 0x05 : Message

Sent by the server to display a message to the client

| type        | name    | description
|-------------|---------|-------------
| MessageType | type    | the type of the message
| string      | sender  | sender of the message **(sent only if type is User)**
| string      | message | the message to display
| boolean     | customStyle | if false, none of the following fields are sent
| MessaageModifier | modifier | defines the way the message should be displayed
| byte        | red     | red color component
| byte        | green   | green color component
| byte        | blue    | blue color component

### 0x06 : Set Phase

Sets the phase of the current game. Usually sent when the game phase changes or when the player joins a game.

| type      | name  | description
|-----------|-------|-------------
| GamePhase | phase | the current phase of the game

### 0x07 : Set Role

Sets the role of the player in the current game. Usually sent when the player role is changed.

| type | name | description
|------|------|-------------
| Role | role | the new role of the player

### 0x08 : Set state

Sets the state of the current game. Usually sent when the game state changes or when the player joins a game.

| type      | name  | description
|-----------|-------|-------------
| GameState | state | the current state of the game

### 0x09 : Set players

Sent when the player list changes

| type     | name       | description
|----------|------------|-------------
| byte     | maxPlayers | the maximum amount of players in the game
| string[] | players    | the list of the players in the game

### 0x0A : Vote end

Sent when a vote ends

| type | name   | description
|------|--------|-------------
| int  | voteId | the id of the ending vote

### 0x0B : Vote request

Sent when a vote starts

| type     | name             | description
|----------|------------------|-------------
| int      | voteId           | the id of the starting vote
| string   | chooseReason     | the "name" of the vote (example : choose a role [for the thief], do you want to heal this player ? [for the witch])
| int      | chooseTime       | the time (in seconds) you have to vote
| string[] | availableChoices | the list of the choices available in this vote
| string[] | voters           | the list of the players who can vote for this vote

### 0x0C : Vote value

Sent to all the voters when a player votes for a value

| type   | name             | description
|--------|------------------|-------------
| int    | voteId           | the id of the vote
| string | votingPlayer     | the player who voted
| string | vote             | the value of the player vote

### 0x0D : Game Composition

Sent to a player when the game composition changes, and when the client joins a game

| type   | name             | description
|--------|------------------|-------------
| Role[] | roles            | list of the available roles to play

### 0x00 : Login

Sent after connecting to a server. Any packet sent before this packet will be refused an lead to the deconnexion of the client.

| type     | name             | description
|----------|------------------|-------------
| short    | protocolVersion  | version of the protocol of the client
| string   | username         | the username the player wants to use on the server
| boolean  | enforceAuth      | whether to use or not the RSA authentication. If false, none of the following fields is defined.
| long     | timestamp        | the timestamp of the authentication request (in milliseconds) (see below for more details on authentication)
| string   | publicKey        | the public key used by the user (see below for more details on authentication)
| byte[]   | signature        | the signature, used to check if the user has the private key (see below for more details on authentication)

#### Authentification procedure

You have to generate a couple of RSA keys (public & private).
When you send a key, it has to be encoded as follow : modulus/exponent
Both modulus and exponent are encoded in base 36 (using all the numbers + the alphabet)

To generate a signature, you have to sign using RSA / SHA256 the following message :

    ==AuthTest==<username>==<public key>==<timestamp>

Example :

    ==AuthTest==zyuiop==modulus/exponent==152456151445

The timestamp is sent in milliseconds.

### 0xA1 : Create Game

This packet is sent to create a game

| type     | name             | description
|----------|------------------|-------------
| string   | name             | the name of the game
| byte     | players          | the maximal amount of players
| Role[]   | characters       | the different characters involved in this game
| string   | password         | the password required to join the game, or null for no password

### 0xA2 : Join Game

This packet is sent to join a game

| type   | name     | description
|--------|----------|---------------
| int    | gameId   | the id of the game to join
| string | password | the password of the game, or null for no password

### 0xA3 : Vote

This packet is sent to vote (in a vote started by 0x0A)

| type   | name   | description
|------- |--------|---------------
| int    | voteId | the id of the vote
| string | choice | the value of the vote

### 0xA4 : Refresh game list

Sent to get the list of the available games.
*This packet is empty*

### 0xA5 : Send Message

Sends a message in the game chat.
The message might be refused if the player hasn't got the right to speak, so you might disable chat depending on the role and the current game phase.
In game, only players can send messages, and only during the day. If a player is a wolf, he can also send a message during Night, and his message will only be seen by the other wolves.

| type   | name    | description
|------- |---------|---------------
| string | message | the message to send

### 0xA6 : Edit composition

This packet is sent to change the composition of the game the client is in

| type     | name             | description
|----------|------------------|-------------
| byte     | players          | the maximal amount of players
| Role[]   | characters       | the different characters involved in this game