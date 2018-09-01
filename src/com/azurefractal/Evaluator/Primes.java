package com.azurefractal.Evaluator;

public class Primes {
	private static int[] values_;

	/*
	 * * each of the thirteen card ranks has its own prime number** deuce = 2*
	 * three = 3* four = 5* five = 7* six = 11* seven = 13* eight = 17* nine =
	 * 19* ten = 23* jack = 29* queen = 31* king = 37* ace = 41
	 */
	static {
		values_ = new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41 };
	}

	public static int[] values() {
		return values_;
	}
}