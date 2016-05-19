package net.zyuiop.loupgarou.protocol.packets.serverbound;

import com.google.common.base.Preconditions;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class CreateGamePacket extends Packet {
	private String name;
	private short    players;
	private Role[] characters;

	public CreateGamePacket() {
	}

	public CreateGamePacket(String name, short players, Role... characters) {
		Preconditions.checkArgument(players < 256, "Players has to be lower than 256");
		this.name = name;
		this.players = players;
		this.characters = characters;
	}

	@Override
	public void read(PacketData byteBuf) {
		name = byteBuf.readString();
		players = byteBuf.readUnsignedByte();
		characters = byteBuf.readArray(Role.class, () -> byteBuf.readEnum(Role.class));
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeString(name);
		byteBuf.writeByte(players);
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

	public Role[] getCharacters() {
		return characters;
	}

	public void setCharacters(Role[] characters) {
		this.characters = characters;
	}
}
