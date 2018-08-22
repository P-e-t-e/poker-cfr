package com.azurefractal;

public class Ranges {

    public static int[][] get_canonical_range() {
        int[][] range = new int[169][2];
        int k = 0;
        PokerCard card0;
        PokerCard card1;
        for (int i = 2; i < 15; i++) {
            card0 = new PokerCard(new Rank(i), Suit.CLUB);
            card1 = new PokerCard(new Rank(i), Suit.DIAMOND);
            range[k][0] = card0.getEncodedValue();
            range[k][1] = card1.getEncodedValue();
            k += 1;

            for (int j = 2; j < i; j++) {
                card0 = new PokerCard(new Rank(i), Suit.CLUB);
                card1 = new PokerCard(new Rank(i), Suit.DIAMOND);
                range[k][0] = card0.getEncodedValue();
                range[k][1] = card1.getEncodedValue();
                k += 1;
                card0 = new PokerCard(new Rank(i), Suit.CLUB);
                card1 = new PokerCard(new Rank(i), Suit.CLUB);
                range[k][0] = card0.getEncodedValue();
                range[k][1] = card1.getEncodedValue();
                k += 1;
            }
        }
        return range;
    }

    public static int[][] get_broadway_range() {
        int[][] range = new int[25][2];
        int k = 0;
        PokerCard card0;
        PokerCard card1;
        for (int i = 10; i < 15; i++) {
            card0 = new PokerCard(new Rank(i), Suit.CLUB);
            card1 = new PokerCard(new Rank(i), Suit.DIAMOND);
            range[k][0] = card0.getEncodedValue();
            range[k][1] = card1.getEncodedValue();
            k += 1;

            for (int j = 10; j < i; j++) {
                card0 = new PokerCard(new Rank(i), Suit.CLUB);
                card1 = new PokerCard(new Rank(i), Suit.DIAMOND);
                range[k][0] = card0.getEncodedValue();
                range[k][1] = card1.getEncodedValue();
                k += 1;
                card0 = new PokerCard(new Rank(i), Suit.CLUB);
                card1 = new PokerCard(new Rank(i), Suit.CLUB);
                range[k][0] = card0.getEncodedValue();
                range[k][1] = card1.getEncodedValue();
                k += 1;
            }
        }
        return range;
    }

    public static int[][] get_leduc_range() {
        int[][] range = new int[6][2];
        int k = 0;
        PokerCard card0;
        PokerCard card1;
        for (int i = 9; i < 15; i++) {
            card0 = new PokerCard(new Rank(i), Suit.CLUB);
            card1 = new PokerCard(new Rank(i), Suit.DIAMOND);
            range[k][0] = card0.getEncodedValue();
            range[k][1] = card1.getEncodedValue();
            k += 1;
        }
        return range;
    }

    public static int[][] get_kuhn_range() {
        int[][] range = new int[3][2];
        int k = 0;
        PokerCard card0;
        PokerCard card1;
        for (int i = 12; i < 15; i++) {
            card0 = new PokerCard(new Rank(i), Suit.CLUB);
            card1 = new PokerCard(new Rank(i), Suit.DIAMOND);
            range[k][0] = card0.getEncodedValue();
            range[k][1] = card1.getEncodedValue();
            k += 1;
        }
        return range;
    }

    public static int[][] get_binary_range() {
        int[][] range = new int[2][2];
        int k = 0;
        PokerCard card0;
        PokerCard card1;
        for (int i = 13; i < 15; i++) {
            card0 = new PokerCard(new Rank(i), Suit.CLUB);
            card1 = new PokerCard(new Rank(i), Suit.DIAMOND);
            range[k][0] = card0.getEncodedValue();
            range[k][1] = card1.getEncodedValue();
            k += 1;
        }
        return range;
    }
}