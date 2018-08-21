package com.azurefractal;

import javax.lang.model.type.NullType;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;

public class Trainer {
    public static final String[] ACTION_NAMES = {"p", "b", "c"};
    public static final int NUM_ACTIONS = 3;
    private static final int[][] RANGES = Ranges.get_kuhn_range();
    private static final int[] board = {PokerCard.to_int("2s"), PokerCard.to_int("4h"), PokerCard.to_int("6s")};
    public static final int NUM_CARDS = RANGES.length;
    private static final int RELATIVE_BET_SIZE = 1;
    private static final Random random = new Random(0);
    public TreeMap<String, Node> nodeMap = new TreeMap<String, Node>();
    public Node rootNode = new Node(new boolean[]{true, true, false}, "");
    private static final int INF = 999999;

//    public double calculateNetExploitability() {
//        return 0.5 * (calculateExploitabilityFor(0) + calculateExploitabilityFor(1));
//    }
//
//    public double calculateExploitabilityFor(int exploiter) {
//        assignReachProbabilities();
//
//        double exploitative_value = 0.0;
//        for (int c = 0; c < NUM_CARDS; c++) {
//            double card_value = findNodeValue("", exploiter, c) / NUM_CARDS;
//            exploitative_value += card_value;
////            System.out.print("Value while holding card:");
////            System.out.println(c);
////            System.out.println(card_value);
//        }
////        System.out.println("Value in total:");
////        System.out.println(exploitative_value);
//        return exploitative_value;
//    }
//
//    private double findNodeValue(String history, int exploiter, int card) {
//        int player = history.length() % 2;
//
////        System.out.print("findNodeValue was called with: ");
////        System.out.println(history);
//
//        String infoSet;
//        if (player == exploiter) {
//            // It's our turn. Let's exploit.
//            infoSet = card + history;
//            Node node = nodeMap.get(infoSet);
//            if (node == null) {
//                System.out.println("Null node encountered");
//                System.out.println(card + history);
//                return 0.0;
//            }
//            // Base case: terminal node. Find showdown value
//            if (node.is_terminal) {
//                int victim = (exploiter + 1) % 2;
////                System.out.println("Exploiter's turn");
////                System.out.println(infoSet);
////                System.out.println(Arrays.deepToString(node.reach_prob));
//                double expected_value = 0.0;
//                double normalization = 0.0000000000001;
//                for (int victim_card = 0; victim_card < NUM_CARDS; victim_card++) {
////                    System.out.println(determineShowdownValue(history, card, victim_card));
//                    int card0 = (exploiter == 0) ? card : victim_card;
//                    int card1 = (exploiter == 1) ? card : victim_card;
//                    // Note the plus sign due to player not being exploiter.
//                    expected_value += 1 * determineShowdownValue(history, card, victim_card) * node.reach_prob[card0][card1];
//                    normalization += node.reach_prob[card0][card1];
//                }
//                return expected_value / normalization;
//            }
//            // Non base case: Find the best choice and take it.
//            double[] values = new double[NUM_ACTIONS];
//
//            for (int a = 0; a < NUM_ACTIONS; a++) {
//                if (node.validActions[a]) {
//                    String nextHistory = history + ACTION_NAMES[a];
//                    values[a] = findNodeValue(nextHistory, exploiter, card);
//                } else {
//                    values[a] = -Trainer.INF;
//                }
//            }
////            System.out.print("Our choice values: ");
////            System.out.print(infoSet);
////            System.out.println(Arrays.toString(values));
//            return Arrays.stream(values).max().orElseThrow(() -> new IllegalArgumentException("Array is empty"));
//        } else {
//            // It's the victim's turn. Consider all possible states the victim could be in:
//            double expected_value = 0.0;
//            double normalization = 0.0000000000001;
//            Node testNode = nodeMap.get(card + history);
//            if (testNode == null) {
//                System.out.println("Null node encountered");
//                System.out.println(card + history);
//                return 0.0;
//            }
//            if (testNode.is_terminal) {
//                for (int victim_card = 0; victim_card < NUM_CARDS; victim_card++) {
//                    // Not sure if this check is sufficient or general
//                    if (victim_card != card) {
//                        infoSet = victim_card + history;
//                        Node node = nodeMap.get(infoSet);
//                        if (node == null) {
//                            System.out.println("Null node encountered");
//                            System.out.println(card + history);
//                            return 0.0;
//                        }
////                        System.out.print(card);
////                        System.out.print(victim_card);
////                        System.out.println("Victim's turn");
////                        System.out.println(infoSet);
////                        System.out.println(Arrays.deepToString(node.reach_prob));
////                        System.out.println(determineShowdownValue(history, victim_card, card));
//                        int card0 = (exploiter == 0) ? card : victim_card;
//                        int card1 = (exploiter == 1) ? card : victim_card;
//                        // Note the minus sign due to player not being exploiter.
//                        expected_value += -1 * determineShowdownValue(history, victim_card, card) * node.reach_prob[card0][card1];
//                        normalization += node.reach_prob[card0][card1];
//                    }
//                }
//            } else {
//                double[] values = new double[NUM_ACTIONS];
//                for (int victim_card = 0; victim_card < NUM_CARDS; victim_card++) {
//                    infoSet = victim_card + history;
//                    Node node = nodeMap.get(infoSet);
//                    double[] strategy = node.getActualStrategy();
//
//                    //Find value of a node like 0p, which is the weighted sum of 0pb, 0pp, etc.
//                    for (int a = 0; a < NUM_ACTIONS; a++) {
//                        String nextHistory = history + ACTION_NAMES[a];
//                        if (node.validActions[a]) {
//                            double value = findNodeValue(nextHistory, exploiter, card);
//                            values[a] = value;
//
//                            int card0 = (exploiter == 0) ? card : victim_card;
//                            int card1 = (exploiter == 1) ? card : victim_card;
//                            expected_value += values[a] * node.reach_prob[card0][card1] * strategy[a];
//                            normalization += node.reach_prob[card0][card1] * strategy[a];
//                        }
//                    }
//                }
//            }
////            System.out.print("V_Call Non base return: ");
////            System.out.println(expected_value / normalization);
////            System.out.println(normalization);
//            return expected_value / normalization;
//        }
//    }

    private double determineShowdownValue(String history, int player_card, int opp_card) {
        int plays = history.length();
        int n_calls = 0;
        char delimiter = 'c';
        for (int i = 0; i < history.length(); n_calls += (history.charAt(i++) == delimiter ? 1 : 0)) ;

        int winSize = (n_calls * (1 * RELATIVE_BET_SIZE) + 1);
        boolean terminalPass = history.charAt(plays - 1) == 'p';
        boolean terminalCall = history.charAt(plays - 1) == 'c';
        String endingString = history.substring(plays - 2, plays);

        int[] player_hole_cards = RANGES[player_card];
        int[] opp_hole_cards = RANGES[opp_card];

        boolean isPlayerCardHigher = HandEvaluator.evaluateHandToInt(board[0], board[1], board[2], player_hole_cards[0], player_hole_cards[1])
                < HandEvaluator.evaluateHandToInt(board[0], board[1], board[2], opp_hole_cards[0], opp_hole_cards[1]);

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
        System.out.println(history);
        return 0.0;
    }

//    private void assignReachProbabilities() {
//        for (Node n : nodeMap.values()) {
//            n.reach_prob = new double[NUM_CARDS][NUM_CARDS];
//        }
//        for (int card0 = 0; card0 < NUM_CARDS; card0++) {
//            for (int card1 = 0; card1 < NUM_CARDS; card1++) {
////                assignNodeReachProb("", card0, card1, 1.0 / NUM_CARDS, 1.0 / NUM_CARDS);
//                if (card0 != card1) {
//                    assignNodeReachProb("", card0, card1, 1.0 / (NUM_CARDS * (NUM_CARDS - 1)));
//                }
//            }
//        }
//    }
//
//    private void assignNodeReachProb(String history, int card0, int card1, double prob) {
//        // Assign the reach probability
//        int player = history.length() % 2;
//        // On even turns, it is plyr0's turn to act. On odd turns, it is plyr1's turn to act.
//        String infoSet = (player == 0) ? card0 + history : card1 + history;
//        Node node = nodeMap.get(infoSet);
//
//        if (node == null) {
//            System.out.println("Null node encountered");
//            System.out.println(infoSet);
//            return;
//        } else {
//            node.reach_prob[card0][card1] += prob;
//        }
//
//        // Base case: terminal node
//        if (node.is_terminal) {
//            return;
//        }
//
//        double[] strategy = node.getActualStrategy();
//
//        for (int a = 0; a < NUM_ACTIONS; a++) {
//            if (node.validActions[a]) {
//                String nextHistory = history + ACTION_NAMES[a];
//                if (player == 0) {
//                    assignNodeReachProb(nextHistory, card0, card1, prob * strategy[a]);
//                } else {
//                    assignNodeReachProb(nextHistory, card0, card1, prob * strategy[a]);
//                }
//            }
//        }
//
//    }

    public void train(int iterations) {
        int[] cards = java.util.stream.IntStream.rangeClosed(0, NUM_CARDS - 1).toArray();
        double[] value0 = new double[NUM_CARDS];
        double[] value1 = new double[NUM_CARDS];
        Tree.buildTree(rootNode, nodeMap);
//        for (Node n : nodeMap.values()) {
//            System.out.println(n);
//        }

        double[] pi = Util.arrayFull(1.0 / NUM_CARDS, NUM_CARDS);
        double[] pni = Util.arrayFull(1.0 / NUM_CARDS, NUM_CARDS);

        //Repeat <iterations> times
        for (int i = 0; i < iterations; i++) {
            for (int plyr_i = 0; plyr_i < 2; plyr_i++) {
                if (plyr_i == 0) {
                    value0 = Util.arrayAdd(value0, cfr("", pi, pni, plyr_i));
                } else {
                    value1 = Util.arrayAdd(value1, cfr("", pi, pni, plyr_i));
                }
            }

            if (i % 1000 == 0 && i != 0) {
                System.out.print("Net Expl:");
                System.out.println(i);
//                System.out.println(calculateNetExploitability());
                System.out.println("Average game value 0: " + Arrays.toString(Util.arrayMultC(1.0 / i, value0)));
                System.out.println("Average game value 1: " + Arrays.toString(Util.arrayMultC(1.0 / i, value1)));
                for (Node n : nodeMap.values()) {
                    if (!n.is_terminal) {
//                        System.out.println(n);
                        System.out.print(n.infoSet);
//                        System.out.println(Arrays.deepToString(n.getStrategy()));
                        System.out.println(Arrays.deepToString(n.regretSum));
                    }
                }
            }
        }
    }

    //This is a recursive function that returns game value
    private double[] cfr(String history, double[] pi, double[] pni, int plyr_i) {
        int plays = history.length();
        int n_calls = 0;
        char delimiter = 'c';
        for (int i = 0; i < history.length(); n_calls += (history.charAt(i++) == delimiter ? 1 : 0)) ;
        int player = plays % 2;
        int opponent = 1 - player;
        int plyr_not_i = 1 - plyr_i;

        Node node = nodeMap.get(history);
//        System.out.println(plyr_i);
//        System.out.println(history);
//        System.out.println(Arrays.toString(pi));
//        System.out.println(Arrays.toString(pni));
        node.p[0] = (plyr_i == 0) ? pi : pni;
        node.p[1] = (plyr_i == 1) ? pi : pni;
        if (node.is_terminal) {
            double[] nodeValue = new double[NUM_CARDS];
            for (int pic = 0; pic < NUM_CARDS; pic++) {
                double opponentUnblockedSum = 0;
                double opponentTotalSum = 0;

                int oppCount = 0;
                for (int pnic = 0; pnic < NUM_CARDS; pnic++) {
                    if (pic != pnic) {
                        nodeValue[pic] += node.p[plyr_not_i][pnic] * (player == plyr_i ? determineShowdownValue(history, pic, pnic) : -determineShowdownValue(history, pnic, pic))
                        ;
                        opponentUnblockedSum += node.p[plyr_not_i][pnic];
                        oppCount += 1;
                    }
                    opponentTotalSum += node.p[plyr_not_i][pnic];
                }
//                System.out.println(Arrays.deepToString(node.p));
//                if (opponentUnblockedSum > 0) {
//                    nodeValue[pic] *= opponentTotalSum / opponentUnblockedSum;
//                }
                if (oppCount > 0) {
                    nodeValue[pic] /= oppCount;
                }
            }
            return nodeValue;
        }

        double[] nodeValue = vncfrGetValue(history, node, pi, pni, plyr_i);

        return nodeValue;
    }

    private double[] vncfrGetValue(String history, Node node, double[] pi, double[] pni, int plyr_i) {
        // LINE 20, 21
        int player = history.length() % 2;
        double[] nodeValue = new double[NUM_CARDS];
        double[][] strategy = node.getStrategy();

        if (plyr_i == 1) {
            int asd = 0;
        }
        // LINE 22
        for (int a = 0; a < NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                String nextHistory = history + ACTION_NAMES[a];
                if (player == plyr_i) {
                    // LINE 24-27
                    node.values[a] = cfr(nextHistory, Util.arrayDot(strategy[a], pi), pni, plyr_i);
                    nodeValue = Util.arrayAdd(nodeValue, Util.arrayDot(strategy[a], node.values[a]));
                } else {
                    // LINE 29-31
//                    System.out.println(Arrays.deepToString(strategy));
//                    System.out.println(Arrays.toString(strategy[a]));
//                    System.out.println(Arrays.toString(Util.arrayDot(strategy[a], pni)));
                    node.values[a] = cfr(nextHistory, pi, Util.arrayDot(strategy[a], pni), plyr_i);
//                    nodeValue = Util.arrayAdd(nodeValue, Util.arrayDot(strategy[a], node.values[a]));
                    nodeValue = Util.arrayAdd(nodeValue, node.values[a]);
                }
            }
        }

        // LINE 34-42
        if (player == plyr_i) {
            for (int c = 0; c < NUM_CARDS; c++) {
                for (int a = 0; a < NUM_ACTIONS; a++) {
                    if (node.validActions[a]) {
                        double regret = 0;
                        regret = node.values[a][c] - nodeValue[c];
                        // According to paper, no weighing of regret here?
                        node.regretSum[a][c] += regret;
//                        node.regretSum[a][c] = Math.max(node.regretSum[a][c] + regret, 0.0);
                        node.strategySum[a][c] += pi[c] * strategy[a][c];
                    }
                }
            }
        }

        return nodeValue;
    }

    public static void main(String[] args) {
        int iterations = 10000;
        Trainer trainer = new Trainer();
        trainer.train(iterations);
        System.out.println("Net Expl:");
//        System.out.println(trainer.calculateNetExploitability());
//        for (Node n : trainer.nodeMap.values()) {
//            System.out.print(n.infoSet);
//            System.out.println(Arrays.deepToString(n.reach_prob));
//        }
//        System.out.println(trainer.determineShowdownValue("bc", 0, 1));
//        System.out.println(trainer.determineShowdownValue("bc", 2, 1));
    }

}