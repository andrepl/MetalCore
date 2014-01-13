package com.norcode.bukkit.metalcore.loottables;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Tier implements ConfigurationSerializable {

	List<PossibleLoot> possibleLoot = new ArrayList<PossibleLoot>();
	int totalWeight = 0;

	public Tier(Map<String, Object> o) {
		possibleLoot = (List<PossibleLoot>) o.get("items");
	}

	public Tier() {
	}

	public PossibleLoot addLoot(ItemStack s, int weight) {
		PossibleLoot loot = new PossibleLoot(s, weight);
		possibleLoot.add(loot);
		totalWeight += weight;
		return loot;
	}

	public void addLoot(PossibleLoot loot) {
		possibleLoot.add(loot);
		totalWeight += loot.getWeight();
	}

	public PossibleLoot getLoot() {
		return WeightedRandom.choose(possibleLoot, totalWeight);
	}

	public ItemStack generateItemStack(Random rand) {
		return WeightedRandom.choose(possibleLoot, totalWeight).generate(rand);
	}

	public List<PossibleLoot> getAllLoot() {
		return possibleLoot;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> o = new HashMap<String, Object>();
		o.put("items", possibleLoot);
		return o;
	}
}
