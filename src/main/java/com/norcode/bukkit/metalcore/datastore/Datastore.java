package com.norcode.bukkit.metalcore.datastore;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface Datastore {

	public void onEnable();
	public DirtyableConfiguration getPlayerData(Player player);
	void flagDirty(DirtyableConfiguration dirtyableConfiguration);
	public void onDisable();

	public Player getOnlinePlayer(UUID id);
	public OfflinePlayer getOfflinePlayer(UUID id);
	public String getPlayerName(UUID id);
	public void setUUID(OfflinePlayer player, UUID id);
}
