package net.zyuiop.loupgarou.server.game.votes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.game.GamePlayer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author zyuiop
 */
public abstract class MajorityVote extends Vote {
	public MajorityVote(int time, String name, Collection<GamePlayer> players, List<String> availableChoices) {
		super(time, name, players, availableChoices);
	}

	public MajorityVote(int time, String name, Collection<GamePlayer> players, String[] availableChoices) {
		super(time, name, players, availableChoices);
	}

	@Override
	protected void handleResults(Map<GamePlayer, String> results) {
		// 1. Add all
		Multimap<String, GamePlayer> voted = HashMultimap.create();
		for (Map.Entry<GamePlayer, String> result : results.entrySet()) {
			voted.put(result.getValue(), result.getKey());
		}

		// 2. Find max
		int max = 0;
		for (Collection<GamePlayer> collection : voted.asMap().values()) {
			if (collection.size() > max)
				max = collection.size();
		}

		// 3. Remove all under max
		Iterator<String> keyIter = voted.keySet().iterator();
		while (keyIter.hasNext()) {
			if (voted.get(keyIter.next()).size() < max)
				keyIter.remove();
		}

		LGServer.getLogger().info(voted.asMap().toString());
		maximalResults(voted);
	}

	protected abstract void maximalResults(Multimap<String, GamePlayer> maximal);
}
