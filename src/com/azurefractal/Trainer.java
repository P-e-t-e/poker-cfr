package com.azurefractal;

import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;

public class Trainer {
    public static final String[] ACTION_NAMES = {"p", "b", "c"};
    public static final int NUM_ACTIONS = 3;
    public static final int RELATIVE_BET_SIZE = 1;
    public static final Random random = new Random();
    public TreeMap<String, Node> nodeMap = new TreeMap<String, Node>();


    class Node {
        //infoset is characterized as cards and history, e.g. "1p" or "3pb"
        String infoSet;
        boolean[] validActions;
        int numValidActions;
        double[] regretSum = new double[NUM_ACTIONS],
                strategy = new double[NUM_ACTIONS],
                strategySum = new double[NUM_ACTIONS];

        Node(boolean[] validActions, String infoSet) {
            this.validActions = validActions;
            this.infoSet = infoSet;
            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (this.validActions[a]) {
                    this.numValidActions += 1;
                }
            }
        }

        //Returns strategy stored by node
        private double[] getStrategy(double realizationWeight) {
            double normalizingSum = 0;
            //For each action, take the strategy weight to be the regret sum if the regret sum is positive. Calculate normalizing sum appropriately.
            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (this.validActions[a]){
                    strategy[a] = regretSum[a] > 0 ? regretSum[a] : 0;
                    normalizingSum += strategy[a];
                }
            }
            //For each action, (if normalizing sum is more than zero, normalize the strategies. Else, set all actions to equal prob).
            //Add the strategy to the strategySum, weighting by realization weight
            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (this.validActions[a]) {
                    if (normalizingSum > 0)
                        strategy[a] /= normalizingSum;
                    else
                        strategy[a] = 1.0 / this.numValidActions;
                    strategySum[a] += realizationWeight * strategy[a];
                }
            }
            return strategy;
        }

        //Returns average strategy stored by node
        public double[] getAverageStrategy() {
            double[] avgStrategy = new double[NUM_ACTIONS];
            double normalizingSum = 0;
            //Calculate normalizing sum. Then, normalize each action and return it. If normalization sum is non-positive, simply return uniform strategy. 
            for (int a = 0; a < NUM_ACTIONS; a++)
                normalizingSum += strategySum[a];
            for (int a = 0; a < NUM_ACTIONS; a++)
                if (normalizingSum > 0)
                    avgStrategy[a] = strategySum[a] / normalizingSum;
                else
                    avgStrategy[a] = 1.0 / this.numValidActions;
            return avgStrategy;
        }

        //Return average strategy
        public String toString() {
            return String.format("%4s: %s", infoSet, Arrays.toString(getAverageStrategy()));
        }

    }


    public void train(int iterations) {
        PokerCard[] board = {PokerCard.parse("2d"), PokerCard.parse("5d"), PokerCard.parse("7d")};
        int[] cards = {1, 2, 3};
        double util = 0;
        //Repeat <iterations> times
        for (int i = 0; i < iterations; i++) {
            //Shuffle cards
            for (int c1 = cards.length - 1; c1 > 0; c1--) {
                int c2 = random.nextInt(c1 + 1);
                int tmp = cards[c1];
                cards[c1] = cards[c2];
                cards[c2] = tmp;
            }
            //Calculate util for each iteration
            util += cfr(cards, "", 1, 1);
        }
        System.out.println("Average game value: " + util / iterations);
        for (Node n : nodeMap.values())
            System.out.println(n);
    }

    //This is a recursive function that returns game utility
    private double cfr(int[] cards, String history, double p0, double p1) {
        int plays = history.length();
        int n_calls = 0;
        char delimiter = 'c';
        for (int i = 0; i < history.length(); n_calls += (history.charAt(i++) == delimiter ? 1 : 0)) ;

        int player = plays % 2;
        int opponent = 1 - player;
        int winSize = n_calls * (1 * RELATIVE_BET_SIZE) + 1;
        //Kuhn poker ends if there has been ((more than 1 move) and (last move is a pass or last 2 moves are bets)). Return utility of game ends. 
        if (plays > 1) {
            boolean terminalPass = history.charAt(plays - 1) == 'p';
            boolean terminalCall = history.charAt(plays - 1) == 'c';
            String endingString = history.substring(plays - 2, plays);
            boolean isPlayerCardHigher = cards[player] > cards[opponent];

            if (terminalPass) {
                if (endingString.equals("bp")) {
                    return winSize;
                } else if (endingString.equals("pp")) {
                    return isPlayerCardHigher ? winSize : -winSize;
                }
            } else if (terminalCall) {
                return isPlayerCardHigher ? winSize : -winSize;
            }
        }

        //infoset is characterized as cards and history, e.g. "1p" or "3pb"
        //Try to get that node. If that node does not exist, create it and put it in the nodeMap under the key infoSet.
        String infoSet = cards[player] + history;
        Node node = nodeMap.get(infoSet);
        if (node == null) {
            boolean[] validActions = {true, infoSet.charAt(infoSet.length() - 1) != 'b',
                    infoSet.charAt(infoSet.length() - 1) == 'b'};
            node = new Node(validActions, infoSet);
            nodeMap.put(infoSet, node);
        }

        //get strategy setting realization weight depending on which player it is now
        double[] strategy = node.getStrategy(player == 0 ? p0 : p1);
        double[] util = new double[NUM_ACTIONS];
        double nodeUtil = 0;
        //For each action, the util is given by the util of cfr with nextHistory, passing in the probably of taking that line
        //Expected node util is given by sum of probabilities of each action times the util of that action 
        for (int a = 0; a < NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                String nextHistory = history + ACTION_NAMES[a];
                util[a] = player == 0
                        ? -cfr(cards, nextHistory, p0 * strategy[a], p1)
                        : -cfr(cards, nextHistory, p0, p1 * strategy[a]);
                nodeUtil += strategy[a] * util[a];
            }
        }

        //For each action, regret is the util of taking action a  minus the node util
        //Add this to the regret sum weighted by probability p1 or p0
        for (int a = 0; a < NUM_ACTIONS; a++) {
            double regret = util[a] - nodeUtil;
            node.regretSum[a] = Math.max(node.regretSum[a] + (player == 0 ? p1 : p0) * regret, 0.0);
//            node.regretSum[a] += (player == 0 ? p1 : p0) * regret;
        }

        return nodeUtil;
    }


    public static void main(String[] args) {
//        int iterations = 1000000;
//        new Trainer().train(iterations);
    }

}