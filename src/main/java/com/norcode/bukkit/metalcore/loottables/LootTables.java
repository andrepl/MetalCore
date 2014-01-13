package com.norcode.bukkit.metalcore.loottables;

import com.norcode.bukkit.metalcore.MetalCorePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class LootTables {

	private static final FilenameFilter YAML_FILES = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".yml");
		}
	};

	private static HashMap<String, ILootTable> tables = new HashMap<String, ILootTable>();
	private static Pattern validNamePattern = Pattern.compile("[A-Za-z0-9_]{2,128}");

	public static void initialize() {
		tables.clear();
		File tableDir = new File(MetalCorePlugin.getMetalCoreDir(), "loot-tables");
		if (!tableDir.isDirectory()) {
			tableDir.mkdirs();
		}
		for (File f: tableDir.listFiles(YAML_FILES)) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
			tables.put(f.getName().substring(0,f.getName().length()-4), (ILootTable) cfg.get("table"));
		}
	}

	public static ILootTable getTable(String name) {
		return tables.get(name);
	}

	public static BasicLootTable createLootTable() {
		BasicLootTable table = new BasicLootTable();
		return table;
	}

	public static void registerLootTable(String name, ILootTable table) {

		if (!validNamePattern.matcher(name).matches()) {
			throw new IllegalArgumentException("Invalid table name.  Loot Table names may only consist of 'A-Za-z0-9_'");
		}
		tables.put(name, table);
	}

	public static void saveLootTables() {
		File tableDir = new File(MetalCorePlugin.getMetalCoreDir(), "loot-tables");
		if (!tableDir.isDirectory()) {
			tableDir.mkdirs();
		}
		for (String s: tables.keySet()) {
			YamlConfiguration cfg = new YamlConfiguration();
			ILootTable t = tables.get(s);
			cfg.set("table", t);
			try {
				cfg.save(new File(tableDir, s + ".yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

