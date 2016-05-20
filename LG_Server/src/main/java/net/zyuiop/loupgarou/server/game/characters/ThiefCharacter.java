package net.zyuiop.loupgarou.server.game.characters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamePlayer;
import net.zyuiop.loupgarou.server.game.votes.Vote;

import java.util.Collection;
import java.util.Map;

/**
 * @author zyuiop
 */
public class ThiefCharacter extends Character {
	protected ThiefCharacter(Game game) {
		super(game);
	}

	private Role[] roles = null;

	public void setRoles(Role[] roles) {
		this.roles = roles;
	}

	@Override
	public void run() {
		GamePlayer thief;
		Collection<GamePlayer> thieves = game.getPlayers(Role.THIEF);
		if (thieves.size() == 0 || roles == null) {
			complete();
			return;
		}

		thief = thieves.iterator().next();
		game.sendToAll(new MessagePacket(MessageType.GAME, "Le voleur se réveille..."));
		thief.sendMessage(MessageType.GAME, "Vous devez désormais choisir votre rôle");

		BiMap<String, Role> roleBiMap = HashBiMap.create();
		for (Role r : roles)
			roleBiMap.put(r.getName(), r);

		Vote vote = new Vote(30, "Choisissez votre rôle", thieves, roleBiMap.keySet()) {
			@Override
			protected void handleResults(Map<GamePlayer, String> results) {
				if (results.entrySet().size() == 0) {
					thief.sendPacket(new MessagePacket(MessageType.GAME, "Vous n'avez choisi aucune autre classe, vous restez donc voleur."));
				} else {
					Map.Entry<GamePlayer, String> entry = results.entrySet().iterator().next();
					Role role = roleBiMap.get(entry.getValue());
					if (role != null)
						thief.setRole(role);
					// TODO : handle null + notice

				}
			}
		};

		vote.setRunAfter(() -> {
			game.sendToAll(new MessagePacket(MessageType.GAME, "Le voleur se rendort..."));
			ThiefCharacter.this.complete();
		});
		vote.run();
	}
}
