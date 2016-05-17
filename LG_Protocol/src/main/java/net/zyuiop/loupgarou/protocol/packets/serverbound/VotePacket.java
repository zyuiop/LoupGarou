package net.zyuiop.loupgarou.protocol.packets.serverbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class VotePacket extends Packet {
	private int    voteId;
	private String vote;

	public VotePacket() {
	}

	public VotePacket(int voteId, String vote) {
		this.voteId = voteId;
		this.vote = vote;
	}

	public int getVoteId() {
		return voteId;
	}

	public String getVote() {
		return vote;
	}

	@Override
	public void read(PacketData byteBuf) {
		voteId = byteBuf.readInt();
		vote = byteBuf.readString();
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeInt(voteId);
		byteBuf.writeString(vote);
	}
}
