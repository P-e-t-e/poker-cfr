package com.azurefractal;

import java.util.Arrays;
import java.util.TreeMap;

public class Node {
    //infoset is characterized as cards and history, e.g. "1p" or "3pb"
    String infoSet;
    boolean[] validActions;
    int numValidActions;
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