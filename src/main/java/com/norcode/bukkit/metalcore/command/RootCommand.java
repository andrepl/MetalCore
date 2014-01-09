package com.norcode.bukkit.metalcore.command;

import com.norcode.bukkit.metalcore.MetalCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;

import java.lang.reflect.Field;
import java.util.Arrays;

public class RootCommand extends BaseCommand {

	private static CommandMap cmap;

	private BukkitCommand cmd;

	public static void initialize() {
		try{
			if(Bukkit.getServer() instanceof CraftServer){
				final Field f = CraftServer.class.getDeclaredField("commandMap");
				f.setAccessible(true);
				cmap = (CommandMap)f.get(Bukkit.getServer());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public RootCommand(MetalCorePlugin plugin, String name, String[] aliases, String requiredPermission, String[] help) {
		super(plugin, name, aliases, requiredPermission, help);
		cmd = new BukkitCommand("name");
		if (aliases != null) {
			cmd.setAliases(Arrays.asList(aliases));
		}
		if (requiredPermission != null) {
			cmd.setPermission(requiredPermission);
		}
		cmd.setExecutor(this);
	}

}
