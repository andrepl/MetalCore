package com.norcode.bukkit.metalcore.loottables;

import net.minecraft.server.v1_7_R1.EnchantmentManager;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PossibleLoot extends WeightedRandomChoice implements IPossibleLoot {

	private final ItemStack stack;
	private float damage;
	private int enchantmentLevel;
	private int qtyMax = 1;
	private int qtyMin = 1;

	public PossibleLoot(Map<String, Object> o) {
		super((Integer) o.get("weight"));
		stack = (ItemStack) o.get("itemstack");
		damage = (Float) o.get("damage");
		enchantmentLevel = (Integer) o.get("enchantment-level");
		qtyMax = (Integer) o.get("max-qty");
		qtyMin = (Integer) o.get("min-qty");
	}

	public PossibleLoot(ItemStack stack, int weight) {
		super(weight);
		this.stack = stack;
		this.qtyMin = stack.getAmount();
	}

	public PossibleLoot setMinQty(int minQty) {
		this.qtyMin = minQty;
		return this;
	}

	public PossibleLoot setMaxQty(int maxQty) {
		this.qtyMax = maxQty;
		return this;
	}

	public PossibleLoot setMinMaxQty(int min, int max) {
		this.qtyMin = min;
		this.qtyMax = max;
		return this;
	}

	public PossibleLoot damage(float damage) {
		this.damage = damage;
		return this;
	}

	public PossibleLoot enchant(int enchantmentLevel) {
		this.enchantmentLevel = enchantmentLevel;
		return this;
	}

	@Override
	public ItemStack generate(Random rand) {
		ItemStack s = stack.clone();
		if (this.damage > 0.0F) {
			int i = (int)(this.damage * this.stack.getType().getMaxDurability());
			int j = stack.getType().getMaxDurability() - (rand.nextInt(i) + 1);
			if (j > i) j = i;
			if (j < 1) j = 1;
			s.setDurability((short) j);
		}
		if (this.enchantmentLevel > 0) {
			s = CraftItemStack.asBukkitCopy(EnchantmentManager.a(rand, CraftItemStack.asNMSCopy(s), 30));
		}
		s.setAmount(rand.nextInt(qtyMax-qtyMin) + qtyMin);
		return s;
	}

	@Override
	public ItemStack getBaseItemstack() {
		return stack;
	}

	public float getDamage() {
		return damage;
	}

	public int getEnchantmentLevel() {
		return enchantmentLevel;
	}

	public int getMaxQty() {
		return qtyMax;
	}

	public int getMinQty() {
		return qtyMin;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> o = new HashMap<String, Object>();
		o.put("enchantment-level", enchantmentLevel);
		o.put("damage", damage);
		o.put("weight", weight);
		o.put("min-qty", qtyMin);
		o.put("max-qty", qtyMax);
		o.put("itemstack", stack);
		return o;
	}
}
