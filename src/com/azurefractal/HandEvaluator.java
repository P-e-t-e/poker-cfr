package com.azurefractal;

/**
 Algorithm by http://suffe.cool/poker/evaluator.html
 */
public class HandEvaluator {
    private static HandRank handRank(int val) {
        if (val > 6185)
            return (HandRank.HIGH_CARD); // 1277 high card
        if (val > 3325)
            return (HandRank.ONE_PAIR); // 2860 one pair
        if (val > 2467)
            return (HandRank.TWO_PAIR); // 858 two pair
        if (val > 1609)
            return (HandRank.THREE_OF_A_KIND); // 858 three-kind
        if (val > 1599)
            return (HandRank.STRAIGHT); // 10 straights
        if (val > 322)
            return (HandRank.FLUSH); // 1277 flushes
        if (val > 166)
            return (HandRank.FULL_HOUSE); // 156 full house
        if (val > 10)
            return (HandRank.FOUR_OF_A_KIND); // 156 four-kind
        return (HandRank.STRAIGHT_FLUSH); // 10 straight-flushes
    }

    private static int find(long u) {
        // The repeated 0xffffffffL is to deal with Java's lack of unsigned int
        int a, b;
        int r;
        u += 0xe91aaa35;
        u &= 0xffffffffL;
        u ^= (u >> 16);
        u &= 0xffffffffL;
        u += (u << 8);
        u &= 0xffffffffL;
        u ^= u >> 4;
        u &= 0xffffffffL;
        b = (int) ((u >> 8) & 0x1ff);
        a = (int) (((u + ((u << 2) & 0xffffffffL)) & 0xffffffffL) >> 19);
        r = (int) (a ^ HashAdjust.values()[b]);
        return r;
    }

    /**
     * Calculates the hash of a specific hand.
     *
     * @param cards Array of PokerCards representing the hand. Uses the first 5.
     * @return The computed rank of the hand..
     */
    public static HandRank evaluateSpecificHand(PokerCard[] cards) {
        return evaluateSpecificHand(cards[0].getEncodedValue(),
                cards[1].getEncodedValue(), cards[2].getEncodedValue(),
                cards[3].getEncodedValue(), cards[4].getEncodedValue());
    }

    /**
     * Calculates the hash of a specific hand.
     *
     * @param card1, card2, card3, card4, card5
     *               PokerCards representing the hand.
     * @return The computed rank of the hand.
     */
    public static HandRank evaluateSpecificHand(int card1, int card2,
                                                int card3, int card4, int card5) {
        int q = (card1 | card2 | card3 | card4 | card5) >> 16;

        // If there is a flush, the fourth nibble will line perfectly on
        // one of the bits.
        if ((card1 & card2 & card3 & card4 & card5 & 0xf000) != 0) {
            return handRank(Flushes.values()[q]); // Flush or straight flush
        }

        short s = Unique5.values()[q]; // check for straights and high card
        // hands
        if (s != 0) {
            return handRank(s);
        }

        return handRank(HashValues.values()[find((long) ((card1 & 0xff)
                * (card2 & 0xff) * (card3 & 0xff) * (card4 & 0xff) * (card5 & 0xff)))]);
    }

    public static int evaluateHandToInt(int card1, int card2,
                                        int card3, int card4, int card5) {
        int q = (card1 | card2 | card3 | card4 | card5) >> 16;

        // If there is a flush, the fourth nibble will line perfectly on
        // one of the bits.
        if ((card1 & card2 & card3 & card4 & card5 & 0xf000) != 0) {
            return Flushes.values()[q]; // Flush or straight flush
        }

        short s = Unique5.values()[q]; // check for straights and high card
        // hands
        if (s != 0) {
            return s;
        }

        return HashValues.values()[find((long) ((card1 & 0xff)
                * (card2 & 0xff) * (card3 & 0xff) * (card4 & 0xff) * (card5 & 0xff)))];
    }

    /**
     * Calculates the hash of a specific hand.
     *
     * @param cards Array of PokerCards representing the hand. Takes in an arbitrary
     *              length array and considers all cards.
     * @return The computed rank of the hand.
     */
    public static int evaluateManyCardHandsToInt(int[] cards) {
        int numCards = cards.length;
        int bestRank = 99999;

        for (int c4 = 4; c4 < numCards; c4++) {
            for (int c3 = 3; c3 < c4; c3++) {
                for (int c2 = 2; c2 < c3; c2++) {
                    for (int c1 = 1; c1 < c2; c1++) {
                        for (int c0 = 0; c0 < c1; c0++) {
//                            System.out.print(c4);
//                            System.out.print(c3);
//                            System.out.print(c2);
//                            System.out.print(c1);
//                            System.out.println(c0);
                            bestRank = Math.min(bestRank,
                                    evaluateHandToInt(cards[c0], cards[c1], cards[c2], cards[c3], cards[c4]));
                        }
                    }
                }
            }
        }
        return bestRank;
    }
}