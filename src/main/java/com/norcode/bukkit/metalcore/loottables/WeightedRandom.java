package com.norcode.bukkit.metalcore.loottables;

import java.util.Collection;
import java.util.Random;

public class WeightedRandom {

	public static Random rand = new Random();

	public static int getTotalWeight(Collection<? extends WeightedRandomChoice> choices) {
		int i = 0;
		for (WeightedRandomChoice choice: choices) {
			i += choice.getWeight();
		}
		return i;
	}

	public static <T extends WeightedRandomChoice> T choose(Collection<T> choices, int maxValue) {
		if (maxValue <= 0) {
			throw new IllegalArgumentException();
		}

		int i = rand.nextInt(maxValue);
		for (T choice: choices) {
			i -= choice.getWeight();
			if (i < 0) {
				return choice;
			}
		}
		return null;
	}

	public static <T extends WeightedRandomChoice> T choose(Collection<T> choices) {
		return choose(choices, getTotalWeight(choices));
	}

}