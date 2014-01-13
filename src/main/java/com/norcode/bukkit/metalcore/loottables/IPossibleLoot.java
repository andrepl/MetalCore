package com.norcode.bukkit.metalcore.loottables;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public interface IPossibleLoot extends ConfigurationSerializable {
	public ItemStack getBaseItemstack();
	public ItemStack generate(Random rand);
	public int getWeight();
}
