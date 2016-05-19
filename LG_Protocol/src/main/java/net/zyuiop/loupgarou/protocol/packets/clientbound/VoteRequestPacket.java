package net.zyuiop.loupgarou.protocol.packets.clientbound;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;

/**
 * @author zyuiop
 */
public class VoteRequestPacket extends Packet {
	private int      voteId; // On peut cumuler deux votes en simul : exemple de la sorcière heal/kill
	private String   chooseReason;
	private int      chooseTime;
	private String[] availableChoices;
	private String[] voters;

	public VoteRequestPacket() {
	}

	public VoteRequestPacket(int voteId, String chooseReason, int chooseTime, String[] availableChoices, String[] voters) {
		this.voteId = voteId;
		this.chooseReason = chooseReason;
		this.chooseTime = chooseTime;
		this.availableChoices = availableChoices;
		this.voters = voters;
	}

	public String getChooseReason() {
		return chooseReason;
	}

	public int getVoteId() {
		return voteId;
	}

	public int getChooseTime() {
		return chooseTime;
	}

	public String[] getAvailableChoices() {
		return availableChoices;
	}

	public String[] getVoters() {
		return voters;
	}

	@Override
	public void read(PacketData byteBuf) {
		voteId = byteBuf.readInt();
		chooseReason = byteBuf.readString();
		chooseTime = byteBuf.readInt();
		availableChoices = byteBuf.readArray(String.class, byteBuf::readString);
		voters = byteBuf.readArray(String.class, byteBuf::readString);
	}

	@Override
	public void write(PacketData byteBuf) {
		byteBuf.writeInt(voteId);
		byteBuf.writeString(chooseReason);
		byteBuf.writeInt(chooseTime);
		byteBuf.writeArray(availableChoices, byteBuf::writeString);
		byteBuf.writeArray(voters, byteBuf::writeString);
	}
}
