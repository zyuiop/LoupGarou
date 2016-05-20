package net.zyuiop.loupgarou.protocol.packets.serverbound;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class ChangeGameCompositionPacket extends Packet {
	private short  players;
	private Role[] characters;

	public ChangeGameCompositionPacket() {
	}

	public ChangeGameCompositionPacket(short players, Role[] characters) {
		this.players = players;
		this.characters = characters;
	}

	@Override
	public void read(PacketData byteBuf) {
		players = byteBuf.readUnsignedByte();
		characters = byteBuf.readArray(Role.class, () -> byteBuf.readEnum(Role.class));
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeByte(players);
		byteBuf.writeArray(characters, byteBuf::writeEnum);
	}

	public int getPlayers() {
		return players;
	}

	public Role[] getCharacters() {
		return characters;
	}

	public void setCharacters(Role[] characters) {
		this.characters = characters;
	}
}
