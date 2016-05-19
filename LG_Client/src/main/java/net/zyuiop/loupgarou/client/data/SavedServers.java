package net.zyuiop.loupgarou.client.data;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.List;

/**
 * @author zyuiop
 */
public class SavedServers {
	private final File file;
	private final Gson                         gson  = new Gson();
	private final TypeToken<List<SavedServer>> token = new TypeToken<List<SavedServer>>() {
	};

	public SavedServers(File workingDirectory) {
		this.file = new File(workingDirectory, "servers.json");
		if (!file.getParentFile().exists()) {
			file.mkdirs();
		}
	}

	public List<SavedServer> getServers() {
		if (!file.exists())
			return Lists.newArrayList();
		try {
			List<SavedServer> list = gson.fromJson(new FileReader(file), token.getType());
			if (list == null)
				return Lists.newArrayList();
			return list;
		} catch (FileNotFoundException e) {
			return Lists.newArrayList();
		}
	}

	public void addServer(SavedServer server) {
		List<SavedServer> servers = getServers();
		servers.add(server);
		write(servers);
	}

	private void write(List<SavedServer> servers) {
		try {
			FileWriter writer = new FileWriter(file);
			gson.toJson(servers, writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeServer(SavedServer server) {
		List<SavedServer> servers = getServers();
		servers.remove(server);
		write(servers);
	}
}
