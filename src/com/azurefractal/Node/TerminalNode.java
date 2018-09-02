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
        char delimiter = 'c';
        for (int i = 0; i < infoSet.length(); n_calls += (infoSet.charAt(i++) == delimiter ? 1 : 0)) ;
        // winSize is half of the pot.
        winSize = Math.pow(1.0 + 2.0 * relativeBetSize, n_calls);
        showdownLost = new BitSet(numCards * numCards);
        validRanges = new BitSet(numCards * numCards);
        int plays = infoSet.length();
        String endingString = infoSet.substring(plays - 2, plays);
        terminalPassPass = endingString.equals("pp");
        terminalFold = endingString.equals("bp");
        terminalCall = infoSet.substring(plays - 1, plays).equals("c");

        calculateShowdownWinner();
        calculateValidRanges();
    }

    private void calculateShowdownWinner() {
        if (!terminalFold) {
            for (int pc = 0; pc < numCards; pc++) {
                for (int oc = 0; oc < numCards; oc++) {
                    if (pc != oc) {
                        showdownLost.set(pc * numCards + oc, !determineShowdownWinner(pc, oc));
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