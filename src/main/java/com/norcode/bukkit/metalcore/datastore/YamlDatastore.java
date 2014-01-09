package com.norcode.bukkit.metalcore.datastore;

import com.norcode.bukkit.metalcore.MetalCorePlugin;
import com.norcode.bukkit.metalcore.util.LRUCache;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YamlDatastore implements Datastore {

	protected MetalCorePlugin plugin;
	protected Map<UUID, String> playerIdMap = new HashMap<UUID, String>();
	protected LRUCache<UUID, DirtyableConfiguration> playerData;
	protected DirtyableSaveQueue saveQueue = new DirtyableSaveQueue();

	public YamlDatastore(MetalCorePlugin plugin) {
		this.plugin = plugin;
	}


	protected File getPlayerIdFile() {
		return new File(MetalCorePlugin.getMetalCoreDir(), "playerid.yml");
	}

	@Override
	public void onEnable() {
		File metalCoreDir = MetalCorePlugin.getMetalCoreDir();
		File pidFile = getPlayerIdFile();
		if (!metalCoreDir.isDirectory()) {
			metalCoreDir.mkdirs();
		}
		if (!pidFile.isFile()) {
			try {
				pidFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(pidFile);
		playerIdMap.clear();
		UUID uuid;
		for (String key: cfg.getKeys(false)) {
			playerIdMap.put(UUID.fromString(key), cfg.getString(key));
		}
		playerData = new LRUCache<UUID, DirtyableConfiguration>(MetalCorePlugin.getMetalCoreConfig().getInt("playerdata-cache"));
	}

	@Override
	public void onDisable() {
		YamlConfiguration cfg = new YamlConfiguration();
		for (Map.Entry<UUID, String> entry: playerIdMap.entrySet()) {
			cfg.set(entry.getKey().toString(), entry.getValue());
		}
		File metalCoreDir = MetalCorePlugin.getMetalCoreDir();
		if (!metalCoreDir.isDirectory()) {
			metalCoreDir.mkdirs();
		}
		File pidFile = getPlayerIdFile();
		try {
			cfg.save(pidFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		File pdatadir = new File(MetalCorePlugin.getMetalCoreDir(), "player-data");
		if (!pdatadir.isDirectory()) {
			pdatadir.mkdirs();
		}
		while (!saveQueue.isEmpty()) {
			DirtyableConfiguration c = saveQueue.poll();
			try {
				c.save(new File(pdatadir, c.getUniqueId().toString() + ".yml"));
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}

	@Override
	public Player getOnlinePlayer(UUID id) {
		String name = getPlayerName(id);
		if (name != null) {
			return plugin.getServer().getPlayerExact(name);
		}
		return null;
	}

	@Override
	public OfflinePlayer getOfflinePlayer(UUID id) {
		String name = getPlayerName(id);
		if (name != null) {
			return plugin.getServer().getOfflinePlayer(name);
		}
		return null;
	}

	@Override
	public String getPlayerName(UUID id) {
		return playerIdMap.get(id);
	}

	@Override
	public void setUUID(OfflinePlayer player, UUID id) {
		playerIdMap.put(id, player.getName());
	}

	@Override
	public DirtyableConfiguration getPlayerData(Player player) {
		UUID playerId = player.getUniqueId();
		DirtyableConfiguration cfg = playerData.get(playerId);
		if (cfg == null) {
			cfg = loadPlayerData(playerId);
			playerData.put(playerId, cfg);
		}
	    return cfg;
	}

	protected DirtyableConfiguration loadPlayerData(UUID playerId) {
		File metalCoreDir = MetalCorePlugin.getMetalCoreDir();
		File playerDataDir = new File(MetalCorePlugin.getMetalCoreDir(), "player-data");
		if (!playerDataDir.isDirectory()) {
			playerDataDir.mkdirs();
		}
		File pFile = new File(playerDataDir, playerId.toString() + ".yml");
		DirtyableConfiguration cfg = new DirtyableConfiguration(this, playerId);
		if (!pFile.isFile()) {
			try {
				pFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			cfg.load(pFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		return cfg;
	}

	@Override
	public void flagDirty(DirtyableConfiguration dirtyableConfiguration) {
		saveQueue.add(dirtyableConfiguration);
	}
}
