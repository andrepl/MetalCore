package com.norcode.bukkit.metalcore.loottables;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public interface ILootTable extends ConfigurationSerializable {
	public IPossibleLoot getPossibleLoot(double d);
	public ItemStack generateItemStack(double d);
}
