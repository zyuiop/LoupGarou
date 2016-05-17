package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class GameStatePacket extends Packet {
	private int      id;
	private String   name;
	private String   hoster;
	private Role[]   roles;
	private String[] players;

	public GameStatePacket() {
	}

	public GameStatePacket(int id, String name, String hoster, Role[] roles, String[] players) {
		this.id = id;
		this.name = name;
		this.hoster = hoster;
		this.roles = roles;
		this.players = players;
	}

	@Override
	public void read(PacketData byteBuf) {
		id = byteBuf.readInt();
		name = byteBuf.readString();
		hoster = byteBuf.readString();
		roles = byteBuf.readArray(Role.class, () -> byteBuf.readEnum(Role.class));
		players = byteBuf.readArray(String.class, byteBuf::readString);
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeInt(id);
		byteBuf.writeString(name);
		byteBuf.writeString(hoster);
		byteBuf.writeArray(roles, byteBuf::writeEnum);
		byteBuf.writeArray(players, byteBuf::writeString);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getHoster() {
		return hoster;
	}

	public Role[] getRoles() {
		return roles;
	}

	public String[] getPlayers() {
		return players;
	}
}
