package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class VoteEndPacket extends Packet {
	private int voteId;

	public VoteEndPacket() {
	}

	public VoteEndPacket(int voteId) {
		this.voteId = voteId;
	}

	public int getVoteId() {
		return voteId;
	}

	@Override
	public void read(PacketData byteBuf) {
		voteId = byteBuf.readInt();
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeInt(voteId);
	}
}
