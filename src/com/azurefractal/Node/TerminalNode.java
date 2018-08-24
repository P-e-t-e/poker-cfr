package com.azurefractal.Node;

import com.azurefractal.HandEvaluator;
import com.azurefractal.Trainer;

import java.util.BitSet;

public class TerminalNode extends Node {
    private boolean terminalPassPass;
    private boolean terminalCall;
    private boolean terminalFold;

    public TerminalNode(boolean[] validActions, String infoSet) {
        super(validActions, infoSet);
        int n_calls = 0;
        char delimiter = 'c';
        for (int i = 0; i < infoSet.length(); n_calls += (infoSet.charAt(i++) == delimiter ? 1 : 0)) ;
        // winSize is half of the pot.
        winSize = (float) Math.pow(1.0 + 2.0 * Trainer.RELATIVE_BET_SIZE, n_calls);
        showdownLost = new BitSet(Trainer.NUM_CARDS * Trainer.NUM_CARDS);
        int plays = infoSet.length();
        String endingString = infoSet.substring(plays - 2, plays);
        terminalPassPass = endingString.equals("pp");
        terminalFold = endingString.equals("bp");
        terminalCall = infoSet.substring(plays - 1, plays).equals("c");

        calculateShowdownWinner();
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

            return HandEvaluator.evaluateHandToInt(Trainer.board[0], Trainer.board[1], Trainer.board[2], player_hole_cards[0], player_hole_cards[1])
                    < HandEvaluator.evaluateHandToInt(Trainer.board[0], Trainer.board[1], Trainer.board[2], opp_hole_cards[0], opp_hole_cards[1]);
        }
        System.out.println("ERROR: The following is not a terminal node");
        System.out.println(infoSet);
        return false;
    }
}