package com.azurefractal;

import com.azurefractal.Evaluator.PokerCard;
import com.azurefractal.Evaluator.Rank;
import com.azurefractal.Evaluator.Suit;

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
                card1 = new PokerCard(new Rank(j), Suit.DIAMOND);
                range[k][0] = card0.getEncodedValue();
                range[k][1] = card1.getEncodedValue();
                k += 1;
                card0 = new PokerCard(new Rank(i), Suit.CLUB);
                card1 = new PokerCard(new Rank(j), Suit.CLUB);
                range[k][0] = card0.getEncodedValue();
                range[k][1] = card1.getEncodedValue();
                k += 1;
            }
        }
        return range;
    }

    public static int[][] get_n_broadway_range(int n) {
        int[][] range = new int[n * n][2];
        int k = 0;
        PokerCard card0;
        PokerCard card1;
        for (int i = 15 - n; i < 15; i++) {
            card0 = new PokerCard(new Rank(i), Suit.CLUB);
            card1 = new PokerCard(new Rank(i), Suit.DIAMOND);
            range[k][0] = card0.getEncodedValue();
            range[k][1] = card1.getEncodedValue();
            k += 1;

            for (int j = 15 - n; j < i; j++) {
                card0 = new PokerCard(new Rank(i), Suit.CLUB);
                card1 = new PokerCard(new Rank(j), Suit.DIAMOND);
                range[k][0] = card0.getEncodedValue();
                range[k][1] = card1.getEncodedValue();
                k += 1;
                card0 = new PokerCard(new Rank(i), Suit.CLUB);
                card1 = new PokerCard(new Rank(j), Suit.CLUB);
                range[k][0] = card0.getEncodedValue();
                range[k][1] = card1.getEncodedValue();
                k += 1;
            }
        }
        return range;
    }

    /**
     * Generates a leduc range containing (AcAh, AdAs, KcKh, KdKs, QcQh, QdQs).
     * The corresponding deck should be (Ac, Ad, Kc, Kd, Qc, Qd).
     * This replicates usual leduc poker with 1 hole card, with the same blocking properties.
     *
     * @return range
     */
    public static int[][] get_leduc_range() {
        int[][] range = new int[6][2];
        int k = 0;
        PokerCard card0;
        PokerCard card1;
        for (int i = 12; i < 15; i++) {
            card0 = new PokerCard(new Rank(i), Suit.CLUB);
            card1 = new PokerCard(new Rank(i), Suit.HEART);
            range[k][0] = card0.getEncodedValue();
            range[k][1] = card1.getEncodedValue();
            k += 1;

            card0 = new PokerCard(new Rank(i), Suit.DIAMOND);
            card1 = new PokerCard(new Rank(i), Suit.SPADE);
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

    public static int[][] get_n_card_deck_range(int n, int[] board) {
        int[] deck = new int[52];
        String[] suits = {"c", "d", "h", "s"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};

        int ctr = 0;
        for (int r = 2; r < 15; r++) {
            for (int s = 0; s < 4; s++) {
                int new_card = PokerCard.to_int(ranks[14 - r] + suits[s]);
                boolean is_clash = false;
                for (int bc = 0; bc < board.length; bc++) {
                    if (board[bc] == new_card) {
                        is_clash = true;
                    }
                }
                if (!is_clash) {
                    deck[ctr] = new_card;
                    ctr += 1;
                }
            }
        }

        int k = 0;
        int valid_n = Math.min(n, ctr);
        int[][] range = new int[(valid_n * (valid_n - 1)) / 2][2];

        for (int i = 0; i < valid_n; i++) {
            for (int j = 0; j < i; j++) {
                range[k][0] = deck[j];
                range[k][1] = deck[i];
                k += 1;
            }
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