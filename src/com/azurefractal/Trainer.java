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

    public double calculateNetExploitability() {
        return 0.5 * (calculateExploitabilityFor(0) + calculateExploitabilityFor(1));
    }

    public double calculateExploitabilityFor(int exploiter) {
        double exploitative_value = 0.0;
        double[] card_value = findNodeValue("", exploiter);
        for (int c = 0; c < NUM_CARDS; c++) {
            exploitative_value += card_value[c] / NUM_CARDS;
//            System.out.print("Value while holding card:");
//            System.out.println(c);
//            System.out.println(card_value);
        }
        System.out.println("Value in total:");
        System.out.println(exploitative_value);
        return exploitative_value;
    }

    private double[] findNodeValue(String history, int exploiter) {
        int player = history.length() % 2;
        int victim = (exploiter + 1) % 2;
        double[] nodeValue = new double[NUM_CARDS];
//        System.out.print("findNodeValue was called with: ");
//        System.out.println(history);

        if (player == exploiter) {
            // It's our turn. Let's exploit.
            Node node = nodeMap.get(history);

            // Base case: terminal node. Find showdown value
            if (node.is_terminal) {
//                System.out.println("Exploiter's turn");
//                System.out.println(history);
//                System.out.println(Arrays.deepToString(node.p));
                for (int pic = 0; pic < NUM_CARDS; pic++) {
                    int oppCount = 0;
                    for (int pnic = 0; pnic < NUM_CARDS; pnic++) {
                        if (pic != pnic) {
                            nodeValue[pic] += node.p[victim][pnic] * determineShowdownValue(history, exploiter, victim);
                            oppCount += 1;
                        }
                    }
                    if (oppCount > 0) {
                        nodeValue[pic] /= oppCount;
                    }
                }
                return nodeValue;
            }

            // Non base case: Find the best choice and take it.
            nodeValue = Util.arrayFull(-Trainer.INF, NUM_CARDS);
            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (node.validActions[a]) {
                    String nextHistory = history + ACTION_NAMES[a];
                    double[] tempValue = findNodeValue(nextHistory, exploiter);
                    for (int ec = 0; ec < NUM_CARDS; ec++) {
                        if (tempValue[ec] > nodeValue[ec]) {
                            nodeValue[ec] = tempValue[ec];
                        }
                    }
                }
            }
            return nodeValue;

        } else {
            // It's the victim's turn. Consider all possible states the victim could be in:
            Node node = nodeMap.get(history);

            if (node.is_terminal) {
                for (int pic = 0; pic < NUM_CARDS; pic++) {
                    int oppCount = 0;
                    for (int pnic = 0; pnic < NUM_CARDS; pnic++) {
                        if (pic != pnic) {
                            // Note the minus sign due to player not being exploiter.
                            nodeValue[pic] += node.p[victim][pnic] * -determineShowdownValue(history, victim, exploiter);
                            oppCount += 1;
                        }
                    }
                    if (oppCount > 0) {
                        nodeValue[pic] /= oppCount;
                    }
                }
                return nodeValue;
            } else {
                double[][] strategy = node.getActualStrategy();
                double normalization[] = Util.arrayFull(0.0000000000001, NUM_CARDS);
                for (int a = 0; a < NUM_ACTIONS; a++) {
                    if (node.validActions[a]) {
                        String nextHistory = history + ACTION_NAMES[a];
                        double[] value = findNodeValue(nextHistory, exploiter);
                        for (int ec = 0; ec < NUM_CARDS; ec++) {
                            for (int vc = 0; vc < NUM_CARDS; vc++) {
                                nodeValue[ec] += value[ec] * strategy[a][vc] * node.p[victim][vc];
                                normalization[ec] += strategy[a][vc] * node.p[victim][vc];
                            }
                        }
                    }
                }
                for (int ec = 0; ec < NUM_CARDS; ec++) {
                    nodeValue[ec] /= normalization[ec];
                }
            }
            return nodeValue;
        }
    }

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
                System.out.println(calculateNetExploitability());
                System.out.println("Average game value 0: " + Arrays.toString(Util.arrayMultC(1.0 / i, value0)));
                System.out.println("Average game value 1: " + Arrays.toString(Util.arrayMultC(1.0 / i, value1)));
                for (Node n : nodeMap.values()) {
                    if (!n.is_terminal) {
//                        System.out.println(n);
                        System.out.print(n.infoSet);
//                        System.out.println(Arrays.deepToString(n.getStrategy()));
                        System.out.println(Arrays.deepToString(n.p));
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
                        nodeValue[pic] += node.p[plyr_not_i][pnic] *
                                (player == plyr_i ?
                                        determineShowdownValue(history, pic, pnic) :
                                        -determineShowdownValue(history, pnic, pic));
                        opponentUnblockedSum += node.p[plyr_not_i][pnic];
                        oppCount += 1;
                    }
                    opponentTotalSum += node.p[plyr_not_i][pnic];
                }

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
//                        node.regretSum[a][c] += regret;
                        node.regretSum[a][c] = Math.max(node.regretSum[a][c] + regret, 0.0);
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