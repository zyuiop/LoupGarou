package net.zyuiop.loupgarou.protocol.packets.serverbound;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class CreateGamePacket extends Packet {
	private String name;
	private int    players;
	private Role[] characters;

	public CreateGamePacket() {
	}

	public CreateGamePacket(String name, int players, Role... characters) {
		this.name = name;
		this.players = players;
		this.characters = characters;
	}

	@Override
	public void read(PacketData byteBuf) {
		name = byteBuf.readString();
		players = byteBuf.readInt();
		characters = byteBuf.readArray(Role.class, () -> byteBuf.readEnum(Role.class));
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeString(name);
		byteBuf.writeInt(players);
		byteBuf.writeArray(characters, byteBuf::writeEnum);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPlayers() {
		return players;
	}

	public void setPlayers(int players) {
		this.players = players;
	}

	public Role[] getCharacters() {
		return characters;
	}

	public void setCharacters(Role[] characters) {
		this.characters = characters;
	}
}
