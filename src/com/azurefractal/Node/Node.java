package com.azurefractal.Node;

import com.azurefractal.Trainer;

import java.util.Arrays;
import java.util.BitSet;

public class Node {
    //infoset is characterized as cards and infoSet, e.g. "1p" or "3pb"
    public String infoSet;
    public boolean[] validActions;
    public int numValidActions;
    public int player;
    public double winSize;
    public BitSet showdownLost;
    public int[] newBoardCards;
    public BitSet validRanges;
    public double[][] regretSum, strategy, strategySum;
    public double[][] values;
    public double[][] p;
    public Node parent_node;
    public Node[] childNodes;
    public boolean is_terminal = false;
    public int numCards;

    public Node(boolean[] validActions, String infoSet, Trainer trainer, int[] newBoardCards) {
        this.validActions = validActions;
        this.infoSet = infoSet;
        numCards = trainer.NUM_CARDS;
        childNodes = new Node[Trainer.NUM_ACTIONS];
        regretSum = new double[Trainer.NUM_ACTIONS][numCards];
        strategy = new double[Trainer.NUM_ACTIONS][numCards];
        strategySum = new double[Trainer.NUM_ACTIONS][numCards];
        p = new double[2][numCards];
        this.newBoardCards = newBoardCards;

        values = new double[Trainer.NUM_ACTIONS][numCards];
        for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
            if (this.validActions[a]) {
                this.numValidActions += 1;
            }
            for (int c = 0; c < numCards; c++) {
                strategySum[a][c] = 0.00001;
            }
        }
        int plays = infoSet.length();
        this.player = plays % 2;

        String endingString1 = (plays > 1) ? infoSet.substring(plays - 1, plays) : "";
        String endingString2 = (plays > 1) ? infoSet.substring(plays - 2, plays) : "";

        if (endingString2.equals("bp") || endingString1.equals("c") || endingString2.equals("pp")) {
            this.is_terminal = true;
        }
    }

    //Returns strategy stored by node
    public double[][] getStrategy() {
        //For each action, take the strategy weight to be the regret sum if the regret sum is positive. Calculate normalizing sum appropriately.
        for (int c = 0; c < numCards; c++) {
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
        double[][] avgStrategy = new double[Trainer.NUM_ACTIONS][numCards];
        for (int c = 0; c < numCards; c++) {
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
//        return getStrategy();
    }

    //Return average strategy
    public String toString() {
        return String.format("%4s: %s", infoSet, Arrays.deepToString(getActualStrategy()));
    }

    // Get showdown value
    public double getShowdownValue(int player_card, int opp_card) {
        return (showdownLost.get(player_card * numCards + opp_card) ? -winSize : winSize);
    }

    public boolean getShowdownWinner(int player_card, int opp_card) {
        return !showdownLost.get(player_card * numCards + opp_card);
    }
}