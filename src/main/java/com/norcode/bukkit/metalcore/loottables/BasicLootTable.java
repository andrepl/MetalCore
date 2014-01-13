package com.norcode.bukkit.metalcore.loottables;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BasicLootTable implements ILootTable {

	List<Tier> tiers = new ArrayList<Tier>();
	public static Random rand = new Random();

	public BasicLootTable() {}

	public BasicLootTable(Map<String, Object> obj) {
		tiers = (List<Tier>) obj.get("tiers");
	}

	public Tier addTier() {
		Tier t = new Tier();
		tiers.add(t);
		return t;
	}

	public Tier getTier(int tier) {
		return tiers.get(tier);
	}

	public Tier chooseTier(double d) {
		int tier = (int) (tiers.size() * d);
		return tiers.get(tier);
	}

	public ItemStack generateItemStack(double d) {
		return chooseTier(d).generateItemStack(rand);
	}

	private List<String> sorted(Set<String> keys) {
		List<String> sorted = new ArrayList<String>(keys);
		Collections.sort(sorted);
		return sorted;
	}

	public PossibleLoot getPossibleLoot(double d) {
		return chooseTier(d).getLoot();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> o = new HashMap<String, Object>();
		o.put("tiers", tiers);
		return null;
	}
}
