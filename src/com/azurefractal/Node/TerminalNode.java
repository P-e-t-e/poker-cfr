package com.azurefractal.Node;

import com.azurefractal.HandEvaluator;
import com.azurefractal.Trainer;
import com.azurefractal.Util;

import java.util.BitSet;

public class TerminalNode extends Node {
    private boolean terminalPassPass;
    private boolean terminalCall;
    private boolean terminalFold;

    public TerminalNode(boolean[] validActions, String infoSet, int[] newBoardCards) {
        super(validActions, infoSet);
        int n_calls = 0;
        char delimiter = 'c';
        for (int i = 0; i < infoSet.length(); n_calls += (infoSet.charAt(i++) == delimiter ? 1 : 0)) ;
        // winSize is half of the pot.
        winSize = (float) Math.pow(1.0 + 2.0 * Trainer.RELATIVE_BET_SIZE, n_calls);
        showdownLost = new BitSet(Trainer.NUM_CARDS * Trainer.NUM_CARDS);
        validRanges = new BitSet(Trainer.NUM_CARDS * Trainer.NUM_CARDS);
        int plays = infoSet.length();
        String endingString = infoSet.substring(plays - 2, plays);
        terminalPassPass = endingString.equals("pp");
        terminalFold = endingString.equals("bp");
        terminalCall = infoSet.substring(plays - 1, plays).equals("c");

        this.newBoardCards = newBoardCards;
        calculateShowdownWinner();
        calculateValidRanges();
    }

    private void calculateShowdownWinner() {
        if (!terminalFold) {
            for (int pc = 0; pc < Trainer.NUM_CARDS; pc++) {
                for (int oc = 0; oc < Trainer.NUM_CARDS; oc++) {
                    if (pc != oc) {
                        showdownLost.set(pc * Trainer.NUM_CARDS + oc, !determineShowdownWinner(pc, oc));
                    }
                }
            }
        }
    }

    private boolean determineShowdownWinner(int player_card, int opp_card) {
        if (terminalPassPass || terminalCall) {
            int[] player_hole_cards = Trainer.RANGES[player_card];
            int[] opp_hole_cards = Trainer.RANGES[opp_card];

            return HandEvaluator.evaluateManyCardHandsToInt(Util.arrayConcatenate(Trainer.board, player_hole_cards)) <
                    HandEvaluator.evaluateManyCardHandsToInt(Util.arrayConcatenate(Trainer.board, opp_hole_cards));
        }
        System.out.println("ERROR: The following is not a terminal node");
        System.out.println(infoSet);
        return false;
    }

    private void calculateValidRanges() {
        for (int pc = 0; pc < Trainer.NUM_CARDS; pc++) {
            for (int oc = 0; oc < Trainer.NUM_CARDS; oc++) {
                if (Util.checkCardNotBlocked(Trainer.RANGES[pc], newBoardCards) &&
                        Util.checkCardNotBlocked(Trainer.RANGES[oc], newBoardCards)) {
                    validRanges.set(pc * Trainer.NUM_CARDS + oc, true);
                }
            }
        }
    }
}