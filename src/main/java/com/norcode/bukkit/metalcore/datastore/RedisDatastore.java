package com.norcode.bukkit.metalcore.datastore;

import com.norcode.bukkit.metalcore.MetalCorePlugin;
import com.norcode.bukkit.metalcore.util.LRUCache;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RedisDatastore extends YamlDatastore {

	public RedisDatastore(MetalCorePlugin metalCorePlugin) {
		super(metalCorePlugin);
	}

	@Override
	public void onEnable() {
		Jedis j = MetalCorePlugin.getJedisPool().getResource();
		Map<String, String> playersIds = j.hgetAll(Bukkit.getServerId() + ":player-ids");
		playerIdMap.clear();
		for (Map.Entry<String, String> e: playersIds.entrySet()) {
			playerIdMap.put(UUID.fromString(e.getKey()), e.getValue());
		}
		MetalCorePlugin.getJedisPool().returnResource(j);
		playerData = new LRUCache<UUID, DirtyableConfiguration>(MetalCorePlugin.getMetalCoreConfig().getInt("playerdata-cache"));
	}

	@Override
	public void onDisable() {
		Map<String, String> pidMap = new HashMap<String, String>();
		for (Map.Entry<UUID, String> entry: playerIdMap.entrySet()) {
	        pidMap.put(entry.getKey().toString(), entry.getValue());
		}
		Jedis j = MetalCorePlugin.getJedisPool().getResource();
		j.del(Bukkit.getServerId() + ":player-ids");
		j.hmset(Bukkit.getServerId() + ":player-ids", pidMap);
		MetalCorePlugin.getJedisPool().returnResource(j);
		while (!saveQueue.isEmpty()) {
			DirtyableConfiguration c = saveQueue.poll();
			String pKey = Bukkit.getServerId() + ":player-data:" + c.getUniqueId().toString();
			Map<String, String> pdata = new HashMap<String, String>();
			for (String key: c.getKeys(false)) {
				pdata.put(key, ((DirtyableSection) c.getConfigurationSection(key)).saveToString());
			}
			j.del(pKey);
			j.hmset(pKey, pdata);
		}
		MetalCorePlugin.getJedisPool().returnResource(j);
	}

	@Override
	protected DirtyableConfiguration loadPlayerData(UUID playerId) {
		DirtyableConfiguration cfg = new DirtyableConfiguration(this, playerId);
		Jedis j = MetalCorePlugin.getJedisPool().getResource();
		Map<String, String> values = j.hgetAll(Bukkit.getServerId() + ":player-data:" + playerId.toString());
		MetalCorePlugin.getJedisPool().returnResource(j);
		for (Map.Entry<String, String> e: values.entrySet()) {
			DirtyableSection sect = (DirtyableSection) cfg.createSection(e.getKey());
			try {
				sect.loadFromString(e.getKey());
			} catch (InvalidConfigurationException e1) {
				e1.printStackTrace();
			}
		}
		return cfg;
	}
}
