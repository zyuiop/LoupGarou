package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class SetRolePacket extends Packet {
	private Role role;

	public SetRolePacket(Role role) {
		this.role = role;
	}

	public SetRolePacket() {
	}

	public Role getRole() {
		return role;
	}

	@Override
	public void read(PacketData byteBuf) {
		role = byteBuf.readEnum(Role.class);
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeEnum(role);
	}
}
