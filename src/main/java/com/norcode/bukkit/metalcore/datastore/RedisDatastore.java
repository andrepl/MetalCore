package com.norcode.bukkit.metalcore.datastore;

import com.norcode.bukkit.metalcore.MetalCorePlugin;
import com.norcode.bukkit.metalcore.util.LRUCache;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

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
		saveTask = new BukkitRunnable() {
			@Override
			public void run() {
				final Map<String, Map<String, String>> toSave = new HashMap<String, Map<String, String>>();
				for (int i=0; i < 5 && !saveQueue.isEmpty(); i++) {
					DirtyableConfiguration cfg = saveQueue.poll();
					toSave.put(Bukkit.getServerId() + ":player-data:" + cfg.getUniqueId().toString(), preparePlayerData(cfg));
					cfg.setDirty(false);
				}
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					@Override
					public void run() {
						Jedis j = MetalCorePlugin.getJedisPool().getResource();
						Pipeline p = j.pipelined();
						for (Map.Entry<String, Map<String, String>> entry: toSave.entrySet()) {
							p.del(entry.getKey());
							p.hmset(entry.getKey(), entry.getValue());
						}
						p.sync();
						MetalCorePlugin.getJedisPool().returnResource(j);
					}
				});
				Jedis j = MetalCorePlugin.getJedisPool().getResource();
				Pipeline p = j.pipelined();
				p.sync();
				MetalCorePlugin.getJedisPool().returnResource(j);
			}
		};
		saveTask.runTaskTimer(plugin, 20, 20);
	}


	public Map<String, String> preparePlayerData(DirtyableConfiguration cfg) {
		Map<String, String> pdata = new HashMap<String, String>();
		for (String key: cfg.getKeys(false)) {
			pdata.put(key, ((DirtyableSection) cfg.getConfigurationSection(key)).saveToString());
		}
		return pdata;
	}

	@Override
	public void onDisable() {
		saveTask.cancel();
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
			Map<String, String> pdata = preparePlayerData(c);
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
