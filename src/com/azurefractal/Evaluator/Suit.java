package com.azurefractal.Evaluator;

/**
 * Keeps track of suits as needed by the rest of the algorithm.
 * 
 * The integer values inside are for the nibble math for determining flushes.
 */
public enum Suit {
	CLUB(0x8000, "C"), 
	DIAMOND(0x4000, "D"), 
	HEART(0x2000, "H"), 
	SPADE(0x1000, "S");

	private final int intValue;

	private final String strValue;

	private Suit(int intValue, String strValue) {
		this.intValue = intValue;
		this.strValue = strValue;
	}

	public int getIntValue() {
		return intValue;
	}

	@Override
	public String toString() {
		return strValue;
	}

	/**
	 * Gets the suit from the character passed in. Legal values are 'c', 'd',
	 * 'h', and 's'.
	 * @param from The character to decode.
	 * @return The corresponding suit or null.
	 */
	public static Suit parse(char from) {
		switch (from) {
		case 'C':
		case 'c':
			return CLUB;
		case 'D':
		case 'd':
			return DIAMOND;
		case 'H':
		case 'h':
			return HEART;
		case 'S':
		case 's':
			return SPADE;
		}
		
		return null;
	}
}
