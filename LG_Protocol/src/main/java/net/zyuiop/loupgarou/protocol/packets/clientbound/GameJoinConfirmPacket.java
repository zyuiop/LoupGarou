package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class GameJoinConfirmPacket extends Packet {
	private int    id;
	private String name;
	private String hoster;

	public GameJoinConfirmPacket() {
	}

	public GameJoinConfirmPacket(int id, String name, String hoster) {
		this.id = id;
		this.name = name;
		this.hoster = hoster;
	}

	@Override
	public void read(PacketData byteBuf) {
		id = byteBuf.readInt();
		name = byteBuf.readString();
		hoster = byteBuf.readString();
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeInt(id);
		byteBuf.writeString(name);
		byteBuf.writeString(hoster);
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
}
