package com.azurefractal;

import com.azurefractal.Evaluator.PokerCard;

import java.util.Arrays;

public class Decks {

    public static int[] generateRemainingDeck(int[] board) {
        int[] deck = new int[52 - board.length];
        String[] suits = {"c", "d", "h", "s"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};

        int ctr = 0;
        for (int r = 2; r < 15; r++) {
            for (int s = 0; s < 4; s++) {
                int new_card = PokerCard.to_int(ranks[14 - r] + suits[s]);
                boolean is_clash = false;
                for (int board_card : board) {
                    if (board_card == new_card) {
                        is_clash = true;
                    }
                }
                if (!is_clash) {
                    deck[ctr] = new_card;
                    ctr += 1;
                }
            }
        }
        System.out.println(Arrays.toString(deck));
        return deck;
    }

    public static int[] generateLeducDeck(int[] board) {
        int[] deck = new int[52 - board.length];
        String[] suits = {"c", "d"};
        String[] ranks = {"Q", "K", "A"};

        int ctr = 0;
        for (int r = 0; r < 3; r++) {
            for (int s = 0; s < 2; s++) {
                int new_card = PokerCard.to_int(ranks[r] + suits[s]);
                boolean is_clash = false;
                for (int board_card : board) {
                    if (board_card == new_card) {
                        is_clash = true;
                    }
                }
                if (!is_clash) {
                    deck[ctr] = new_card;
                    ctr += 1;
                }
            }
        }
        System.out.println(Arrays.toString(deck));
        return deck;
    }
}
