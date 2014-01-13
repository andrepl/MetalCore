package com.norcode.bukkit.metalcore.loottables;

public abstract class WeightedRandomChoice {

	protected int weight;

	protected WeightedRandomChoice(int weight) {
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}
}
