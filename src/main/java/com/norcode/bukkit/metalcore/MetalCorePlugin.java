package com.norcode.bukkit.metalcore;

import com.norcode.bukkit.metalcore.command.RootCommand;
import com.norcode.bukkit.metalcore.datastore.Datastore;
import com.norcode.bukkit.metalcore.datastore.DirtyableConfiguration;
import com.norcode.bukkit.metalcore.datastore.RedisDatastore;
import com.norcode.bukkit.metalcore.datastore.YamlDatastore;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.PacketPlayOutChat;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetalCorePlugin extends JavaPlugin {

	protected boolean debugMode;
	protected static boolean debugMetalCore = true;

	private static Economy vaultEconomy;
	private static Permission vaultPermission;
	private static Chat vaultChat;
	private static JedisPool jedisPool;
	private static MetalCoreLogger logger;
	protected static List<MetalCorePlugin> instances = new ArrayList<MetalCorePlugin>();
	private static Datastore datastore;
	private static FileConfiguration metalCoreConfig;

	public static FileConfiguration getMetalCoreConfig() {
		return metalCoreConfig;

	}

	public static Datastore getDatastore() {
		return datastore;
	}

	@Override
	public void onEnable() {
		instances.add(this);
		if (instances.size() == 1) {
			logger = new MetalCoreLogger();
			debug("initializing...");
			configureMetalcore();
			RootCommand.initialize();
		}
		saveDefaultConfig();
		reloadConfig();
	}

	private void configureMetalcore() {
		MetalCorePlugin.getMetalCoreDir().mkdirs();
		File metalCoreConfigFile = new File(MetalCorePlugin.getMetalCoreDir(), "config.yml");
		if (!metalCoreConfigFile.isFile()) {
			try {
				metalCoreConfigFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// set defaults
		metalCoreConfig = YamlConfiguration.loadConfiguration(metalCoreConfigFile);
		metalCoreConfig.addDefault("debug-mode", false);
		metalCoreConfig.addDefault("playerdata-cache", 100);
		metalCoreConfig.addDefault("datastore", "yaml");

		debugMetalCore = metalCoreConfig.getBoolean("debug-mode");

		// setup datastore
		if (metalCoreConfig.getString("datastore").equalsIgnoreCase("yaml")) {
			datastore = new YamlDatastore(this);
		} else if (metalCoreConfig.getString("datastore").equalsIgnoreCase("redis")) {
			datastore = new RedisDatastore(this);
		}
		getServer().getPluginManager().registerEvents(new LoginListener(), this);
	}

	public static JedisPool getJedisPool() {
		if (jedisPool == null) {
			JedisPoolConfig poolConfig = new JedisPoolConfig();
			jedisPool = new JedisPool(poolConfig, "localhost", 6379, 0);
		}
		return jedisPool;
	}

	public Jedis getJedis() {
		return jedisPool.getResource();
	}

	public void returnJedis(Jedis j) {
		jedisPool.returnResource(j);
	}

	public void returnBrokenJedis(Jedis j) {
		jedisPool.returnBrokenResource(j);
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			vaultPermission = permissionProvider.getProvider();
		}
		return (vaultPermission != null);
	}

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
		if (chatProvider != null) {
			vaultChat = chatProvider.getProvider();
		}

		return (vaultChat != null);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			vaultEconomy = economyProvider.getProvider();
		}

		return (vaultEconomy != null);
	}

	public void onDisable() {
		if (instances.size() == 1) {
			// the final instance of a metalcore plugin is disabling shut down everything
			debug("disabling...");
			vaultChat = null;
			vaultEconomy = null;
			vaultPermission = null;
			datastore.onDisable();
		}
		instances.remove(this);

	}

	public void reloadConfig() {
		super.reloadConfig();
		debugMode = getConfig().getBoolean("debug-mode");
	}

	public static void send(CommandSender player, IChatBaseComponent... lines) {
		for (IChatBaseComponent comp : lines) {
			if (!(player instanceof Player)) {
				player.sendMessage(comp.e());
			} else {
				PacketPlayOutChat packet = new PacketPlayOutChat(comp, true);
				((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	public static void send(CommandSender player, String... lines) {
		player.sendMessage(lines);
	}

	public static MetalCorePlugin getInstance() {
		return instances.get(0);
	}

	public static void debug(Object o) {
		if (debugMetalCore && logger != null) {
			logger.info(o.toString());
		}
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public Economy getEconomy() {
		return vaultEconomy;
	}

	public Permission getPermissions() {
		return vaultPermission;
	}

	public Chat getChat() {
		return vaultChat;
	}

	public static File getMetalCoreDir() {
		return new File(getInstance().getDataFolder().getParent(), "MetalCore");
	}

	public ConfigurationSection getPlayerData(Player p) {
		DirtyableConfiguration cfg = datastore.getPlayerData(p);
		if (!cfg.contains(getName())) {
			return cfg.createSection(getName());
		}
		return cfg.getConfigurationSection(getName());
	}
}
