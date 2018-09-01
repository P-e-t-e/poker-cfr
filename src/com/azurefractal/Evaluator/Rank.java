package com.azurefractal.Evaluator;

/**
 * Convenience class to hold ranks and convert easily.
 */
public class Rank {
	public static final int MIN_RANK = 2;  // DEUCE
	public static final int MAX_RANK = 14; // ACE
	
	private int rank;
	
	public Rank(int rank) {
		this.rank = rank;
	}
	
	public int getRank() {
		return rank;
	}
	
	@Override
	public String toString() {
        switch (rank) {
        case 10: // TEN
            return "T";
        case 11: // JACK
            return "J";
        case 12: // QUEEN
            return "Q";
        case 13: // KING
            return "K";
        case 14: // ACE
        	return "A";
        default:
            return Integer.toString(rank);
        }
	}
	
	/**
	 * Convert a character to a Rank.
	 * @param from The encoded character 2-9, T, J, Q, K, and A.
	 * @return The corresponding rank.
	 */
	public static Rank parse(char from) {
		
		switch(from) {
        case 'T':
        case 't':
        	return new Rank(10);
        case 'J':
        case 'j':
        	return new Rank(11);
        case 'Q':
        case 'q':
        	return new Rank(12);
        case 'K':
        case 'k':
        	return new Rank(13);
        case 'A':
        case 'a':
        	return new Rank(14);
        default:
            return new Rank(Character.getNumericValue(from));
		}
	}
}
