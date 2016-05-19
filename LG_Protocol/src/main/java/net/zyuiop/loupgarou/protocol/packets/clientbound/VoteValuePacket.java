package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class VoteValuePacket extends Packet {
	private int id;
	private String votingPlayer;
	private String vote;

	public VoteValuePacket() {
	}

	public VoteValuePacket(int id, String votingPlayer, String vote) {
		this.id = id;
		this.votingPlayer = votingPlayer;
		this.vote = vote;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getVotingPlayer() {
		return votingPlayer;
	}

	public void setVotingPlayer(String votingPlayer) {
		this.votingPlayer = votingPlayer;
	}

	public String getVote() {
		return vote;
	}

	public void setVote(String vote) {
		this.vote = vote;
	}

	@Override
	public void read(PacketData byteBuf) {
		id = byteBuf.readInt();
		votingPlayer = byteBuf.readString();
		vote = byteBuf.readString();
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeInt(id);
		byteBuf.writeString(votingPlayer);
		byteBuf.writeString(vote);
	}
}
