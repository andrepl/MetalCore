package com.norcode.bukkit.metalcore;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LoginListener implements Listener {
	@EventHandler(ignoreCancelled=true, priority= EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		MetalCorePlugin.getDatastore().setUUID(event.getPlayer(), event.getPlayer().getUniqueId());
	}
}
