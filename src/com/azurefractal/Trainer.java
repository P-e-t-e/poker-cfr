package com.azurefractal;

import com.azurefractal.Node.*;

import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;

public class Trainer {
    public static final String[] ACTION_NAMES = {"p", "b", "c"};
    public static final int NUM_ACTIONS = 3;
    public static final int[] board = {PokerCard.to_int("2s"), PokerCard.to_int("4h"), PokerCard.to_int("6s")};
    public static final int[][] RANGES = Ranges.get_n_card_deck_range(52, board);
    public static final int NUM_CARDS = RANGES.length;
    public static final int NUM_BOARD_CARDS = 1;
    public static final double RELATIVE_BET_SIZE = 0.5;
    public static final int BETS_LEFT = 3;
    public static final Random random = new Random(0);
    public TreeMap<String, Node> nodeMap = new TreeMap<>();
    public Node rootNode = new Node(new boolean[]{true, true, false}, "");
    public static final int INF = 999999;

    public double calculateNetExploitability() {
        return 0.5 * (calculateExploitabilityFor(0) + calculateExploitabilityFor(1));
    }

    public double calculateExploitabilityFor(int exploiter) {
        double[] pi = Util.arrayFull(1.0 / NUM_CARDS, NUM_CARDS);
        double[] pni = Util.arrayFull(1.0 / NUM_CARDS, NUM_CARDS);
        double[] card_value = exploit(rootNode, pi, pni, exploiter);
        double exploitative_value = 0.0;
        for (int c = 0; c < NUM_CARDS; c++) {
            exploitative_value += card_value[c];
        }
        System.out.print("Game Value:");
        System.out.println(exploitative_value);
        System.out.println("Value while holding card:");
        System.out.println(Arrays.toString(card_value));
        return exploitative_value;
    }

    public void train(int iterations, int render_intvl) {
        System.out.println(RANGES.length);
        System.out.println(Arrays.deepToString(RANGES));
        double[] value0 = new double[NUM_CARDS];
        double[] value1 = new double[NUM_CARDS];
        Tree.buildTree(rootNode, nodeMap);
//        for (Node n : nodeMap.values()) {
//            System.out.print(n.infoSet);
//            System.out.println(Arrays.toString(n.validActions));
//        }

        double[] pi = Util.arrayFull(1.0 / NUM_CARDS, NUM_CARDS);
        double[] pni = Util.arrayFull(1.0 / NUM_CARDS, NUM_CARDS);

        //Repeat <iterations> times
        for (int i = 0; i < iterations; i++) {
            for (int plyr_i = 0; plyr_i < 2; plyr_i++) {
                if (plyr_i == 0) {
                    value0 = Util.arrayAdd(value0, cfr(rootNode, pi, pni, plyr_i));
                } else {
                    value1 = Util.arrayAdd(value1, cfr(rootNode, pi, pni, plyr_i));
                }
            }

            if (i % render_intvl == 0 && i != 0) {
                System.out.print("Net Expl:");
                System.out.println(i);
                System.out.println(calculateNetExploitability());
                System.out.println("Average game value 0: " + Double.toString(Util.arraySum(Util.arrayMultC(1.0 / i, value0))));
                System.out.println("Average game value 1: " + Double.toString(Util.arraySum(Util.arrayMultC(1.0 / i, value1))));
                for (Node n : nodeMap.values()) {
                    if (!n.is_terminal) {
//                        System.out.println(n);
//                        System.out.print(n.infoSet);
//                        System.out.println(Arrays.deepToString(n.getActualStrategy()));
//                        System.out.println(Arrays.deepToString(n.p));
                    }
                }
            }
        }
    }

    //This is a recursive function that returns game value
    private double[] cfr(Node node, double[] pi, double[] pni, int plyr_i) {
        int player = node.player;
        int plyr_not_i = 1 - plyr_i;

        node.p[0] = (plyr_i == 0) ? pi : pni;
        node.p[1] = (plyr_i == 1) ? pi : pni;

        if (node instanceof BoardNode) {
            int bc = random.nextInt(NUM_BOARD_CARDS);
            Node nextNode = node.childNodes[bc];
            return cfr(nextNode, pi, pni, plyr_i);
        } else if (node instanceof TerminalNode) {
            return find_terminal_node_value(node, player, plyr_i, plyr_not_i);
        }

        // LINE 20, 21
        double[] nodeValue = new double[NUM_CARDS];
        double[][] strategy = node.getStrategy();

        // LINE 22
        for (int a = 0; a < NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                Node nextNode = node.childNodes[a];
                if (player == plyr_i) {
                    // LINE 24-27
                    node.values[a] = cfr(nextNode, Util.arrayDot(strategy[a], pi), pni, plyr_i);
                    nodeValue = Util.arrayAdd(nodeValue, Util.arrayDot(strategy[a], node.values[a]));
                } else {
                    // LINE 29-31
                    node.values[a] = cfr(nextNode, pi, Util.arrayDot(strategy[a], pni), plyr_i);
//                    nodeValue = Util.arrayAdd(nodeValue, Util.arrayDot(strategy[a], node.values[a]));
                    nodeValue = Util.arrayAdd(nodeValue, node.values[a]);
                }
            }
        }

        // LINE 34-42
        if (player == plyr_i) {
            update_regret_and_strategy_sum(node, nodeValue, strategy, pi);
        }
        return nodeValue;
    }

    private double[] find_terminal_node_value(Node node, int player, int plyr_i, int plyr_not_i) {
//        long nanoTime = System.nanoTime();
        double[] nodeValue = new double[NUM_CARDS];
        boolean player_is_plyr_i = player == plyr_i;
        double[] p_not_i = node.p[plyr_not_i];
        double winSize = node.winSize;

        for (int pic = 0; pic < NUM_CARDS; pic++) {
//            double opponentUnblockedSum = 0;
//            double opponentTotalSum = 0;
            double nodeValueTemp = 0;
            int oppCount = 0;
            for (int pnic = 0; pnic < NUM_CARDS; pnic++) {
                if (!Util.checkCardBlock(RANGES[pic], RANGES[pnic])) {
                    nodeValueTemp += (player_is_plyr_i ?
                            (node.getShowdownWinner(pic, pnic) ? p_not_i[pnic] : -p_not_i[pnic]) :
                            (node.getShowdownWinner(pnic, pic) ? -p_not_i[pnic] : p_not_i[pnic]));
//                    opponentUnblockedSum += node.p[plyr_not_i][pnic];
                    oppCount += 1;
                }
//                opponentTotalSum += node.p[plyr_not_i][pnic];
            }
//                if (opponentUnblockedSum > 0) {
//                    nodeValue[pic] = nodeValueTemp / opponentUnblockedSum;
//                }
            if (oppCount > 0) {
                nodeValue[pic] = winSize * nodeValueTemp / oppCount;
            }
//                System.out.println(opponentUnblockedSum / opponentTotalSum);
//                System.out.println(oppCount);
        }
//        System.out.println(System.nanoTime() - nanoTime);
        return nodeValue;
    }

    private void update_regret_and_strategy_sum(Node node, double[] nodeValue, double[][] strategy, double[] pi) {
        for (int c = 0; c < NUM_CARDS; c++) {
            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (node.validActions[a]) {
                    double regret = node.values[a][c] - nodeValue[c];
                    // According to paper, no weighing of regret here?
//                        node.regretSum[a][c] += regret;
                    node.regretSum[a][c] = Math.max(node.regretSum[a][c] + regret, 0.0);
                    node.strategySum[a][c] += pi[c] * strategy[a][c];
                }
            }
        }
    }

    private double[] exploit(Node node, double[] pi, double[] pni, int plyr_i) {
        int player = node.player;
        int plyr_not_i = 1 - plyr_i;

        node.p[0] = (plyr_i == 0) ? pi : pni;
        node.p[1] = (plyr_i == 1) ? pi : pni;
        if (node instanceof BoardNode) {
            double[] nodeValue = new double[NUM_CARDS];
            for (int bc = 0; bc < NUM_BOARD_CARDS; bc++) {
                Node nextNode = node.childNodes[bc];
                nodeValue = Util.arrayAdd(nodeValue, Util.arrayMultC(1.0 / NUM_BOARD_CARDS, exploit(nextNode, pi, pni, plyr_i)));
            }
            return nodeValue;
        } else if (node.is_terminal) {
            double[] nodeValue = new double[NUM_CARDS];
            for (int pic = 0; pic < NUM_CARDS; pic++) {
                double opponentUnblockedSum = 0;
                int oppCount = 0;
                for (int pnic = 0; pnic < NUM_CARDS; pnic++) {
                    if (!Util.checkCardBlock(RANGES[pic], RANGES[pnic])) {
                        nodeValue[pic] += node.p[plyr_not_i][pnic] *
                                (player == plyr_i ?
                                        node.getShowdownValue(pic, pnic) :
                                        -node.getShowdownValue(pnic, pic));
                        oppCount += 1;
                    }
                }
//                if (opponentUnblockedSum > 0) {
//                    nodeValue[pic] *= 1 / opponentUnblockedSum;
//                }
                if (oppCount > 0) {
                    nodeValue[pic] /= oppCount;
                }
            }
            return nodeValue;
        }

        return exploitGetValue(node, pi, pni, plyr_i);
    }

    private double[] exploitGetValue(Node node, double[] pi, double[] pni, int plyr_i) {
        // LINE 20, 21
        int player = node.player;
        double[] nodeValue = new double[NUM_CARDS];
        if (player == plyr_i) {
            nodeValue = Util.arrayFull(-Trainer.INF, NUM_CARDS);
        }
        double[][] strategy = node.getActualStrategy();

        // LINE 22
        for (int a = 0; a < NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                Node nextNode = node.childNodes[a];
                if (player == plyr_i) {
                    // LINE 24-27
                    node.values[a] = exploit(nextNode, Util.arrayDot(strategy[a], pi), pni, plyr_i);
                    for (int c = 0; c < NUM_CARDS; c++) {
                        if (node.values[a][c] > nodeValue[c]) {
                            nodeValue[c] = node.values[a][c];
                        }
                    }
                } else {
                    // LINE 29-31
                    node.values[a] = exploit(nextNode, pi, Util.arrayDot(strategy[a], pni), plyr_i);
//                    nodeValue = Util.arrayAdd(nodeValue, Util.arrayDot(strategy[a], node.values[a]));
                    nodeValue = Util.arrayAdd(nodeValue, node.values[a]);
                }
            }
        }

        return nodeValue;
    }

    public static void main(String[] args) {
        int iterations = 100;
        Trainer trainer = new Trainer();
        trainer.train(iterations, iterations / 10);

//        for (Node n : trainer.nodeMap.values()) {
//            System.out.print(n.infoSet);
//            System.out.println(Arrays.deepToString(n.getActualStrategy()));
//        }
    }

}