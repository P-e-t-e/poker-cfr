package com.azurefractal;

import javax.lang.model.type.NullType;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;

public class Trainer {
    public static final String[] ACTION_NAMES = {"p", "b", "c"};
    public static final int NUM_ACTIONS = 3;
    public static final int NUM_CARDS = 3;
    public static final int RELATIVE_BET_SIZE = 1;
    public static final Random random = new Random();
    public TreeMap<String, Node> nodeMap = new TreeMap<String, Node>();
    public Node rootNode = new Node(new boolean[]{true, true, false}, "");
    public static final int INF = 999999;

    class Node {
        //infoset is characterized as cards and history, e.g. "1p" or "3pb"
        String infoSet;
        boolean[] validActions;
        int numValidActions;
        double[] regretSum = new double[NUM_ACTIONS],
                strategy = new double[NUM_ACTIONS],
                strategySum = new double[NUM_ACTIONS];
        Node parent_node;
        Node child_node;
        boolean is_terminal = false;
        double[][] reach_prob = new double[2][NUM_CARDS];

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
                if (this.validActions[a]) {
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
            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (this.validActions[a]) {
                    normalizingSum += strategySum[a];
                }
            }

            for (int a = 0; a < NUM_ACTIONS; a++)
                if (normalizingSum > 0 && this.validActions[a]) {
                    avgStrategy[a] = strategySum[a] / normalizingSum;
                } else if (this.validActions[a]) {
                    avgStrategy[a] = 1.0 / this.numValidActions;
                }

            return avgStrategy;
        }

        //Return average strategy
        public String toString() {
            return String.format("%4s: %s", infoSet, Arrays.toString(getAverageStrategy()));
        }

    }

    public double calculateExploitability(int exploiter) {
        int[] cards = {0, 1, 2};
        assignReachProbabilities(cards);

        double exploitative_value = 0.0;
        for (int c = 0; c < cards.length; c++) {
            double card_value = findNodeValue("", exploiter, c) / NUM_CARDS;
            exploitative_value += card_value;
            System.out.print("Value while holding card:");
            System.out.println(c);
            System.out.println(card_value);
        }
        System.out.println("Value in total:");
        System.out.println(exploitative_value);
        return exploitative_value;
    }

    private double findNodeValue(String history, int exploiter, int card) {
        int player = history.length() % 2;

        System.out.print("findNodeValue was called with: ");
        System.out.println(history);

        String infoSet;
        if (player == exploiter){
            // It's our turn. Let's exploit.
            infoSet = card + history;
            Node node = nodeMap.get(infoSet);
            // Base case: terminal node. Find showdown value
            if (node.is_terminal) {
                int victim = (exploiter + 1) % 2;
                System.out.println("Exploiter's turn");
                System.out.println(infoSet);
                System.out.println(Arrays.toString(node.reach_prob[exploiter]));
                System.out.println(Arrays.toString(node.reach_prob[victim]));
                double expected_value = 0.0;
                double normalization = 0.0;
                for (int victim_card = 0; victim_card < NUM_CARDS; victim_card++) {
//                    System.out.println(determineShowdownValue(node, exploiter, history, card, victim_card));
                    expected_value += determineShowdownValue(node, exploiter, history, card, victim_card) * node.reach_prob[victim][victim_card];
                    normalization += node.reach_prob[victim][victim_card];
                }
                return expected_value / normalization;
            }
            // Non base case: Find the best choice and take it.
            double[] utils = new double[NUM_ACTIONS];

            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (node.validActions[a]) {
                    String nextHistory = history + ACTION_NAMES[a];
//                    System.out.print("E_Call: ");
//                    System.out.println(nextHistory);
                    utils[a] = findNodeValue(nextHistory, exploiter, card);
                } else {
                    utils[a] = -Trainer.INF;
                }
            }
            System.out.print("Our choice utils: ");
            System.out.print(infoSet);
            System.out.println(Arrays.toString(utils));
            return Arrays.stream(utils).max().orElseThrow(() -> new IllegalArgumentException("Array is empty"));
        } else {
            // It's the victim's turn. Consider all possible states the victim could be in:
            double expected_value = 0.0;
            double normalization = 0.0;
            int victim = (exploiter + 1) % 2;
            Node testNode = nodeMap.get(card + history);
            if (testNode.is_terminal) {
                for (int victim_card = 0; victim_card < NUM_CARDS; victim_card++) {
                    // Not sure if this check is sufficient or general
                    if (victim_card != card){
                        infoSet = victim_card + history;
                        Node node = nodeMap.get(infoSet);
                        System.out.print(card);
                        System.out.print(victim_card);
                        System.out.println("Victim's turn");
                        System.out.println(infoSet);
                        System.out.println(Arrays.toString(node.reach_prob[exploiter]));
                        System.out.println(Arrays.toString(node.reach_prob[victim]));
                        System.out.println(determineShowdownValue(node, exploiter, history, victim_card, card));
                        expected_value += determineShowdownValue(node, exploiter, history, victim_card, card) * node.reach_prob[victim][victim_card];
                        normalization += node.reach_prob[victim][victim_card];
                    }
                }
            } else {
                double[] utils = new double[NUM_ACTIONS];
                for (int a = 0; a < NUM_ACTIONS; a++) {
                    infoSet = card + history + ACTION_NAMES[a];
                    String nextHistory = history + ACTION_NAMES[a];
                    Node node = nodeMap.get(infoSet);

                    if (node != null){
                        System.out.print("Find chance util: ");
                        double util = findNodeValue(nextHistory, exploiter, card);
                        utils[a] = util;
                        expected_value += utils[a] * node.reach_prob[exploiter][card];
                        normalization += node.reach_prob[exploiter][card];
                        System.out.print("Ourr chance utils: ");
                        System.out.print(infoSet);
                        System.out.println(util);
                    }
                }
                System.out.print("Our chance utils: ");
                System.out.print(history);
                System.out.println(Arrays.toString(utils));
            }
            System.out.print("V_Call Non base return: ");
            System.out.println(expected_value / normalization);
            System.out.println(normalization);
            return expected_value / normalization;
        }
    }

    private double determineShowdownValue(Node node, int exploiter, String history, int player_card, int opp_card) {
        int plays = history.length();
        int n_calls = 0;
        char delimiter = 'c';
        for (int i = 0; i < history.length(); n_calls += (history.charAt(i++) == delimiter ? 1 : 0)) ;
        int player = plays % 2;
        boolean is_hero_player = exploiter == player;
        int winSize = (is_hero_player ? 1 : -1) * (n_calls * (1 * RELATIVE_BET_SIZE) + 1);
        boolean terminalPass = history.charAt(plays - 1) == 'p';
        boolean terminalCall = history.charAt(plays - 1) == 'c';
        String endingString = history.substring(plays - 2, plays);

        boolean isPlayerCardHigher = player_card > opp_card;

        if (terminalPass) {
            if (endingString.equals("bp")) {
                return winSize;
            } else if (endingString.equals("pp")) {
                return (isPlayerCardHigher ? winSize : -winSize);
            }
        } else if (terminalCall) {
            return (isPlayerCardHigher ? winSize : -winSize);
        }
        System.out.println("ERROR");
        return 0.0;
    }


    private void assignReachProbabilities(int[] range) {
        for (Node n : nodeMap.values()){
            for (int i = 0; i < range.length; i++){
                n.reach_prob[0][i] = 0;
                n.reach_prob[1][i] = 0;
            }
        }
        for (int card0 = 0; card0 < range.length; card0++) {
            for (int card1 = 0; card1 < range.length; card1++) {
//                assignNodeReachProb("", card0, card1, 1.0 / NUM_CARDS, 1.0 / NUM_CARDS);
                if (card0 != card1){
                    assignNodeReachProb("", card0, card1, 1.0 / 2, 1.0 / 2);
                } else {
                    assignNodeReachProb("", card0, card1, 0.0, 0.0);
                }

            }
        }
    }

    private void assignNodeReachProb(String history, int card0, int card1, double prob0, double prob1) {
        // Assign the reach probability
        int player = history.length() % 2;
        // On even turns, it is plyr0's turn to act. On odd turns, it is plyr1's turn to act.
        String infoSet = (player == 0) ? card0 + history : card1 + history;
        Node node = nodeMap.get(infoSet);

        node.reach_prob[0][card0] += prob0;
        node.reach_prob[1][card1] += prob1;

        // Base case: terminal node
        if (node.is_terminal) {
            return;
        }

        // Non base case
//        double[] strategy = new double[NUM_ACTIONS];
//        if (history.length() % 2 == player) {
//            strategy = node.getAverageStrategy();
//        } else {
//            for (int a = 0; a < NUM_ACTIONS; a++) {
//                strategy[a] = 1;
//            }
//        }
        double[] strategy = node.getAverageStrategy();

        for (int a = 0; a < NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                String nextHistory = history + ACTION_NAMES[a];
                if (player == 0) {
                    assignNodeReachProb(nextHistory, card0, card1, prob0 * strategy[a], prob1 * strategy[a]);
                } else {
                    assignNodeReachProb(nextHistory, card0, card1, prob0 * strategy[a], prob1 * strategy[a]);
                }
            }
        }

    }


    public void train(int iterations) {
        PokerCard[] board = {PokerCard.parse("2d"), PokerCard.parse("5d"), PokerCard.parse("7d")};
        int[] cards = {0, 1, 2};
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
            util += cfr(cards, "", 1, 1, rootNode);
            if (i % 100000 == 0 && i > 30000) {
                calculateExploitability(0);
            }
        }
        System.out.println("Average game value: " + util / iterations);
        for (Node n : nodeMap.values()){
            System.out.println(n);
        }
    }

    //This is a recursive function that returns game utility
    private double cfr(int[] cards, String history, double p0, double p1, Node parent_node) {
        int plays = history.length();
        int n_calls = 0;
        char delimiter = 'c';
        for (int i = 0; i < history.length(); n_calls += (history.charAt(i++) == delimiter ? 1 : 0)) ;
        int player = plays % 2;
        int opponent = 1 - player;
        int winSize = n_calls * (1 * RELATIVE_BET_SIZE) + 1;

        //infoset is characterized as cards and history, e.g. "1p" or "3pb"
        //Try to get that node. If that node does not exist, create it and put it in the nodeMap under the key infoSet.
        String infoSet = cards[player] + history;
        Node node = nodeMap.get(infoSet);
        if (node == null) {
            boolean[] validActions = {true, infoSet.charAt(infoSet.length() - 1) != 'b',
                    infoSet.charAt(infoSet.length() - 1) == 'b'};
            node = new Node(validActions, infoSet);
            node.child_node = parent_node;
            nodeMap.put(infoSet, node);
        }

        //Kuhn poker ends if there has been ((more than 1 move) and (last move is a pass or last 2 moves are bets)). Return utility of game ends. 
        if (plays > 1) {
            boolean terminalPass = history.charAt(plays - 1) == 'p';
            boolean terminalCall = history.charAt(plays - 1) == 'c';
            String endingString = history.substring(plays - 2, plays);
            boolean isPlayerCardHigher = cards[player] > cards[opponent];

            if (terminalPass) {
                if (endingString.equals("bp")) {
                    node.is_terminal = true;
                    return winSize;
                } else if (endingString.equals("pp")) {
                    node.is_terminal = true;
                    return isPlayerCardHigher ? winSize : -winSize;
                }
            } else if (terminalCall) {
                node.is_terminal = true;
                return isPlayerCardHigher ? winSize : -winSize;
            }
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
                        ? -cfr(cards, nextHistory, p0 * strategy[a], p1, node)
                        : -cfr(cards, nextHistory, p0, p1 * strategy[a], node);
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
        int iterations = 500000;
        Trainer trainer = new Trainer();
        trainer.train(iterations);
        trainer.calculateExploitability(0);
        for (Node n : trainer.nodeMap.values()){
            System.out.print(n.infoSet);
            System.out.println(Arrays.deepToString(n.reach_prob));
        }
    }

}