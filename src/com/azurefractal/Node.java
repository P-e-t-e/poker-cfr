package com.azurefractal;

import java.util.Arrays;
import java.util.TreeMap;

public class Node {
    //infoset is characterized as cards and infoSet, e.g. "1p" or "3pb"
    String infoSet;
    boolean[] validActions;
    int numValidActions;
    int player;
    double[][] showdownValue = new double[Trainer.NUM_CARDS][Trainer.NUM_CARDS];
    double[][] regretSum = new double[Trainer.NUM_ACTIONS][Trainer.NUM_CARDS],
            strategy = new double[Trainer.NUM_ACTIONS][Trainer.NUM_CARDS],
            strategySum = new double[Trainer.NUM_ACTIONS][Trainer.NUM_CARDS],
            values = new double[Trainer.NUM_ACTIONS][Trainer.NUM_CARDS],
            p = new double[2][Trainer.NUM_CARDS];
    Node parent_node;
    Node[] childNodes = new Node[Trainer.NUM_ACTIONS];
    boolean is_terminal = false;
    boolean is_boardnode = false;
    int street_number = 0;

    Node(boolean[] validActions, String infoSet, int street_number) {
        this.validActions = validActions;
        this.infoSet = infoSet;
        for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
            if (this.validActions[a]) {
                this.numValidActions += 1;
            }
            for (int c = 0; c < Trainer.NUM_CARDS; c++) {
                strategySum[a][c] = 0.01;
            }
        }
        int plays = infoSet.length();
        this.player = plays % 2;

        String endingString1 = (plays > 1) ? infoSet.substring(plays - 1, plays) : "";
        String endingString2 = (plays > 1) ? infoSet.substring(plays - 2, plays) : "";

        if (endingString2.equals("bp") || endingString1.equals("c") || endingString2.equals("pp")) {
            this.is_terminal = true;
        }
        if (this.is_terminal) {
            calculateShowdownValue();
        }
    }

    public void calculateShowdownValue() {
        for (int pc = 0; pc < Trainer.NUM_CARDS; pc++) {
            for (int oc = 0; oc < Trainer.NUM_CARDS; oc++) {
                if (pc != oc) {
                    showdownValue[pc][oc] = determineShowdownValue(pc, oc);
                }
            }
        }
    }

    private double determineShowdownValue(int player_card, int opp_card) {
        int plays = infoSet.length();
        int n_calls = 0;
        char delimiter = 'c';
        for (int i = 0; i < infoSet.length(); n_calls += (infoSet.charAt(i++) == delimiter ? 1 : 0)) ;

        int winSize = (n_calls * (1 * Trainer.RELATIVE_BET_SIZE) + 1);
        boolean terminalPass = infoSet.charAt(plays - 1) == 'p';
        boolean terminalCall = infoSet.charAt(plays - 1) == 'c';
        String endingString = infoSet.substring(plays - 2, plays);

        int[] player_hole_cards = Trainer.RANGES[player_card];
        int[] opp_hole_cards = Trainer.RANGES[opp_card];

        boolean isPlayerCardHigher = HandEvaluator.evaluateHandToInt(Trainer.board[0], Trainer.board[1], Trainer.board[2], player_hole_cards[0], player_hole_cards[1])
                < HandEvaluator.evaluateHandToInt(Trainer.board[0], Trainer.board[1], Trainer.board[2], opp_hole_cards[0], opp_hole_cards[1]);

        if (terminalPass) {
            if (endingString.equals("bp")) {
                return winSize;
            } else if (endingString.equals("pp")) {
                return (isPlayerCardHigher ? winSize : -winSize);
            }
        } else if (terminalCall) {
            return (isPlayerCardHigher ? winSize : -winSize);
        }
        System.out.println("ERROR: The following is not a terminal node");
        System.out.println(infoSet);
        return 0.0;
    }

    //Returns strategy stored by node
    public double[][] getStrategy() {
        //For each action, take the strategy weight to be the regret sum if the regret sum is positive. Calculate normalizing sum appropriately.
        for (int c = 0; c < Trainer.NUM_CARDS; c++) {
            double normalizingSum = 0;
            for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
                if (this.validActions[a]) {
                    strategy[a][c] = regretSum[a][c] > 0 ? regretSum[a][c] : 0;
                    normalizingSum += strategy[a][c];
                }
            }
            for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
                if (this.validActions[a]) {
                    if (normalizingSum > 0)
                        strategy[a][c] /= normalizingSum;
                    else
                        strategy[a][c] = 1.0 / this.numValidActions;
                }
            }
        }
        return strategy;
    }

    //Returns average strategy stored by node
    public double[][] getAverageStrategy() {
        double[][] avgStrategy = new double[Trainer.NUM_ACTIONS][Trainer.NUM_CARDS];
        for (int c = 0; c < Trainer.NUM_CARDS; c++) {
            double normalizingSum = 0;
            //Calculate normalizing sum. Then, normalize each action and return it. If normalization sum is non-positive, simply return uniform strategy.
            for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
                if (this.validActions[a]) {
                    normalizingSum += strategySum[a][c];
                }
            }

            for (int a = 0; a < Trainer.NUM_ACTIONS; a++)
                if (normalizingSum > 0 && this.validActions[a]) {
                    avgStrategy[a][c] = strategySum[a][c] / normalizingSum;
                } else if (this.validActions[a]) {
                    avgStrategy[a][c] = 1.0 / this.numValidActions;
                }
        }
        return avgStrategy;
    }

    public double[][] getActualStrategy() {
        return getAverageStrategy();
//            return getStrategy(0);
    }

    //Return average strategy
    public String toString() {
        return String.format("%4s: %s", infoSet, Arrays.deepToString(getActualStrategy()));
    }

}