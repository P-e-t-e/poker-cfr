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
        double[] pi = Util.arrayFull(1.0 / NUM_CARDS, NUM_CARDS);
        double[] pni = Util.arrayFull(1.0 / NUM_CARDS, NUM_CARDS);
        double[] card_value = exploit("", pi, pni, exploiter);
        double exploitative_value = 0.0;
        for (int c = 0; c < NUM_CARDS; c++) {
            exploitative_value += card_value[c] / NUM_CARDS;
//            System.out.print("Value while holding card:");
//            System.out.println(c);
//            System.out.println(Arrays.toString(card_value));
        }
        return exploitative_value;
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

            if (i % 100000 == 0 && i != 0) {
                System.out.print("Net Expl:");
                System.out.println(i);
                System.out.println(calculateNetExploitability());
//                System.out.println("Average game value 0: " + Arrays.toString(Util.arrayMultC(1.0 / i, value0)));
//                System.out.println("Average game value 1: " + Arrays.toString(Util.arrayMultC(1.0 / i, value1)));
                for (Node n : nodeMap.values()) {
                    if (!n.is_terminal) {
//                        System.out.println(n);
//                        System.out.print(n.infoSet);
//                        System.out.println(Arrays.deepToString(n.getStrategy()));
//                        System.out.println(Arrays.deepToString(n.p));
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

    private double[] exploit(String history, double[] pi, double[] pni, int plyr_i) {
        int plays = history.length();
        int n_calls = 0;
        char delimiter = 'c';
        for (int i = 0; i < history.length(); n_calls += (history.charAt(i++) == delimiter ? 1 : 0)) ;
        int player = plays % 2;
        int plyr_not_i = 1 - plyr_i;

        Node node = nodeMap.get(history);

        node.p[0] = (plyr_i == 0) ? pi : pni;
        node.p[1] = (plyr_i == 1) ? pi : pni;
        if (node.is_terminal) {
            double[] nodeValue = new double[NUM_CARDS];
            for (int pic = 0; pic < NUM_CARDS; pic++) {

                int oppCount = 0;
                for (int pnic = 0; pnic < NUM_CARDS; pnic++) {
                    if (pic != pnic) {
                        nodeValue[pic] += node.p[plyr_not_i][pnic] *
                                (player == plyr_i ?
                                        determineShowdownValue(history, pic, pnic) :
                                        -determineShowdownValue(history, pnic, pic));
                        oppCount += 1;
                    }
                }

                if (oppCount > 0) {
                    nodeValue[pic] /= oppCount;
                }
            }
            return nodeValue;
        }

        double[] nodeValue = exploitGetValue(history, node, pi, pni, plyr_i);

        return nodeValue;
    }

    private double[] exploitGetValue(String history, Node node, double[] pi, double[] pni, int plyr_i) {
        // LINE 20, 21
        int player = history.length() % 2;
        double[] nodeValue = new double[NUM_CARDS];
        if (player == plyr_i) {
            nodeValue = Util.arrayFull(-Trainer.INF, NUM_CARDS);
        }
        double[][] strategy = node.getStrategy();

        // LINE 22
        for (int a = 0; a < NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                String nextHistory = history + ACTION_NAMES[a];
                if (player == plyr_i) {
                    // LINE 24-27
                    node.values[a] = exploit(nextHistory, Util.arrayDot(strategy[a], pi), pni, plyr_i);
                    for (int c = 0; c < NUM_CARDS; c++) {
                        if (node.values[a][c] > nodeValue[c]) {
                            nodeValue[c] = node.values[a][c];
                        }
                    }
                } else {
                    // LINE 29-31
                    node.values[a] = exploit(nextHistory, pi, Util.arrayDot(strategy[a], pni), plyr_i);
//                    nodeValue = Util.arrayAdd(nodeValue, Util.arrayDot(strategy[a], node.values[a]));
                    nodeValue = Util.arrayAdd(nodeValue, node.values[a]);
                }
            }
        }

        return nodeValue;
    }

    public static void main(String[] args) {
        int iterations = 10000000;
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