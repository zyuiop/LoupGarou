package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class SetGameCompositionPacket extends Packet {
	private Role[] roles;

	@Override
	public void read(PacketData byteBuf) {
		roles = byteBuf.readArray(Role.class, () -> byteBuf.readEnum(Role.class));
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeArray(roles, byteBuf::writeEnum);
	}

	public SetGameCompositionPacket(Role[] roles) {
		this.roles = roles;
	}

	public SetGameCompositionPacket() {
	}

	public Role[] getRoles() {
		return roles;
	}
}
