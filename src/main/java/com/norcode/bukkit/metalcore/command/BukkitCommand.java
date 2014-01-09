package com.norcode.bukkit.metalcore.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BukkitCommand extends Command {

	private BaseCommand executor = null;

	protected BukkitCommand(String name) {
		super(name);
	}

	public boolean execute(CommandSender sender, String commandLabel,String[] args) {
		if(executor != null){
			executor.onCommand(sender, this, commandLabel, args);
		}
		return false;
	}

	public void setExecutor(BaseCommand executor) {
		this.executor = executor;
	}

}
