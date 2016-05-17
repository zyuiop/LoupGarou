package net.zyuiop.loupgarou.server.game;

import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.packets.clientbound.VoteEndPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.VoteRequestPacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.VotePacket;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.network.ProtocolHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zyuiop
 */
public abstract class Vote implements Runnable {
	private static int                nextVoteId = 1;
	private static Map<Integer, Vote> votes      = new ConcurrentHashMap<>();

	private static int getVoteId() {
		if (nextVoteId == Integer.MAX_VALUE) {
			nextVoteId = 1;
		}
		return nextVoteId++;
	}

	public static Vote getVote(int id) {
		return votes.get(id);
	}

	static {
		// Init handler
		ProtocolHandler.handle(VotePacket.class, ((packet, client) -> {
			int voteId = packet.getVoteId();
			Vote vote = getVote(voteId);
			if (vote != null)
				vote.handle(packet, client.getPlayer());
		}));
	}

	public Vote(int time, Game game, String name, Collection<GamePlayer> players, List<String> availableChoices) {
		this(time, game, name, players, availableChoices.toArray(new String[availableChoices.size()]));
	}

	public Vote(int time, Game game, String name, Collection<GamePlayer> players, String[] availableChoices) {
		this.time = time;
		this.game = game;
		this.name = name;
		this.players = players;
		this.availableChoices = availableChoices;
		votes.put(id, this);
		startVote();
	}

	private boolean terminated = false;
	private final int  time;
	private final Game game;
	private final int id = getVoteId();
	private final String                 name;
	private final Collection<GamePlayer> players;
	private final String[] availableChoices;
	protected final Map<GamePlayer, String> votations = new ConcurrentHashMap<>();

	@Override
	public void run() {
		try {
			LGServer.getLogger().info("Vote #" + id + " started, will end in " + time + " seconds.");
			Thread.sleep(time * 1000);
			LGServer.getLogger().info("Vote #" + id + " will end.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		endVote();
	}

	private void endVote() {
		if (terminated)
			return;

		handleResults(votations);
		terminated = true;
		votes.remove(id);
		broadcastPacket(new VoteEndPacket(id));
	}

	private void broadcastPacket(Packet packet) {
		players.forEach(player -> player.getClient().sendPacket(packet));
	}

	public void startVote() {
		LGServer.getInstance().getExecutor().submit(this); // Schedule end
		broadcastPacket(new VoteRequestPacket(id, name, time, availableChoices)); // broadcast packet
	}

	public void handle(VotePacket packet, GamePlayer player) {
		LGServer.getLogger().info("Handle vote for " + id + " from " + player.getName() + " with value " + packet.getVote());

		if (!players.contains(player))
			return;
		if (packet.getVoteId() != id)
			return;
		if (!allowVote(player, packet.getVote()))
			return;

		boolean isIn = false;
		for (String vote : availableChoices) {
			if (vote.equalsIgnoreCase(packet.getVote())) {
				isIn = true;
				break;
			}
		}

		if (!isIn)
			return;
		votations.put(player, packet.getVote());

		if (votations.size() == players.size()) {
			endVote();
		}
	}

	protected boolean allowVote(GamePlayer player, String vote) {
		return true;
	}

	protected abstract void handleResults(Map<GamePlayer, String> results);

	public String getSelectedValue(GamePlayer player) {
		return votations.get(player);
	}
}
