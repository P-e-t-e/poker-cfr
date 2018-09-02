package com.azurefractal.Node;

import com.azurefractal.Evaluator.HandEvaluator;
import com.azurefractal.Trainer;
import com.azurefractal.Util;

import java.util.BitSet;

public class TerminalNode extends Node {
    private boolean terminalPassPass;
    private boolean terminalCall;
    private boolean terminalFold;
    private double relativeBetSize;
    private int[][] ranges;
    private int[] board;

    public TerminalNode(boolean[] validActions, String infoSet, Trainer trainer, int[] newBoardCards) {
        super(validActions, infoSet, trainer, newBoardCards);
        relativeBetSize = trainer.RELATIVE_BET_SIZE;
        ranges = trainer.RANGES;
        board = Util.arrayConcatenate(trainer.board, newBoardCards);

        int n_calls = 0;
        for (int i = 1; i < infoSet.length(); i++) {
            if (infoSet.charAt(i) == 'c') {
                n_calls++;
            } else if ((infoSet.charAt(i) == 'b') && (infoSet.charAt(i - 1) == 'b')) {
                n_calls++;
            }
        }

        // winSize is half of the pot.
        winSize = Math.pow(1.0 + 2.0 * relativeBetSize, n_calls);
        showdownNotWon = new BitSet(numCards * numCards);
        showdownDrawn = new BitSet(numCards * numCards);
        validRanges = new BitSet(numCards * numCards);
        int plays = infoSet.length();
        String endingString = infoSet.substring(plays - 2, plays);
        terminalPassPass = endingString.equals("pp");
        terminalFold = endingString.equals("bp");
        terminalCall = infoSet.substring(plays - 1, plays).equals("c");

        calculateShowdown();
        calculateValidRanges();
    }

    private void calculateShowdown() {
        if (!terminalFold) {
            for (int pc = 0; pc < numCards; pc++) {
                for (int oc = 0; oc < numCards; oc++) {
                    if (pc != oc) {
                        int playerStrength = HandEvaluator.evaluateManyCardHandsToInt(Util.arrayConcatenate(board, ranges[pc]));
                        int oppStrength = HandEvaluator.evaluateManyCardHandsToInt(Util.arrayConcatenate(board, ranges[oc]));
                        showdownNotWon.set(pc * numCards + oc, !(playerStrength < oppStrength));
                        if (playerStrength == oppStrength) {
                            showdownDrawn.set(pc * numCards + oc, true);
                        }
                    }
                }
            }
        }
    }

    private boolean determineShowdownWinner(int player_card, int opp_card) {
        if (terminalPassPass || terminalCall) {
            int[] player_hole_cards = ranges[player_card];
            int[] opp_hole_cards = ranges[opp_card];

            return HandEvaluator.evaluateManyCardHandsToInt(Util.arrayConcatenate(board, player_hole_cards)) <
                    HandEvaluator.evaluateManyCardHandsToInt(Util.arrayConcatenate(board, opp_hole_cards));
        }
        System.out.println("ERROR: The following is not a terminal node");
        System.out.println(infoSet);
        return false;
    }

    private void calculateValidRanges() {
        for (int pc = 0; pc < numCards; pc++) {
            for (int oc = 0; oc < numCards; oc++) {
                if (Util.checkCardNotBlocked(ranges[pc], ranges[oc], newBoardCards)) {
                    validRanges.set(pc * numCards + oc, true);
                }
            }
        }
    }
}