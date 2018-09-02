package com.azurefractal;

import com.azurefractal.Evaluator.PokerCard;
import com.azurefractal.Node.BoardNode;
import com.azurefractal.Node.Node;
import com.azurefractal.Node.TerminalNode;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;

public class Trainer {
    // Board that is out there before the first street
    public int[] board = {PokerCard.to_int("2s"), PokerCard.to_int("4h"),
            PokerCard.to_int("6s"), PokerCard.to_int("8h")};
    // Array of possible two card holdings that either player could have
    public int[][] RANGES = Ranges.get_n_card_deck_range(12, board);//Ranges.get_leduc_range();//
    public int NUM_CARDS = RANGES.length;
    // A 2D array of booleans that is true when the player cards do not block each other
    public boolean[][] VALID_RANGE_PAIRS = Util.InitializeValidRangePairs(RANGES);
    public int[] DECK = Decks.generateRemainingDeck(board);//Decks.generateLeducDeck(board);//
    // Number of possible board cards that could be dealt
    public int NUM_BOARD_CARDS = 48;
    // Number of streets. The number of board cards dealt is NUM_STREETS - 1
    public int NUM_STREETS = 2;
    // Bet size in terms of number of pot
    public double RELATIVE_BET_SIZE = 1.0;
    // Maximum number of pot sized bets that can be placed.
    public int BETS_LEFT = 2;

    public static final String[] ACTION_NAMES = {"p", "b", "c"};
    public static final int NUM_ACTIONS = 3;
    public static final Random random = new Random(0);
    public TreeMap<String, Node> nodeMap = new TreeMap<>();
    public Node rootNode;
    public static final int INF = 999999;
    public double[] result = new double[3];
    public double weight = 1.0;

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

    public double[] train(int iterations, int render_intvl) {
        double[] value0 = new double[NUM_CARDS];
        double[] value1 = new double[NUM_CARDS];
        double exploitability = INF;

        System.out.println(RANGES.length);
        System.out.println(Arrays.deepToString(RANGES));


        rootNode = new Node(new boolean[]{true, true, false}, "", this, new int[]{});
        Tree.buildTree(rootNode, nodeMap, this);
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
            weight += 1.0;

            if (i % render_intvl == 0 && i != 0) {
                System.out.print("Net Expl:");
                System.out.println(i);
                exploitability = calculateNetExploitability();
                System.out.println(exploitability);
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
        result[0] = exploitability;
        result[1] = Util.arraySum(Util.arrayMultC(1.0 / iterations, value0));
        result[2] = Util.arraySum(Util.arrayMultC(1.0 / iterations, value1));
        return result;
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
            node.values[bc] = cfr(nextNode, pi, pni, plyr_i);
            return node.values[bc];
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
            update_regret_and_strategy_sum(node, nodeValue, strategy, pi, weight);
        }
        return nodeValue;
    }

    private double[] find_terminal_node_value(Node node, int player, int plyr_i, int plyr_not_i) {
//        long nanoTime = System.nanoTime();
        double[] nodeValue = new double[NUM_CARDS];
        boolean player_is_plyr_i = player == plyr_i;
        double[] p_not_i = node.p[plyr_not_i];
        double winSize = node.winSize;
        double nodeValueTemp;
        int oppCount;

        for (int pic = 0; pic < NUM_CARDS; pic++) {
//            double opponentUnblockedSum = 0;
//            double opponentTotalSum = 0;
            nodeValueTemp = 0;
            oppCount = 0;
            for (int pnic = 0; pnic < NUM_CARDS; pnic++) {
                if (node.validRanges.get(pic * NUM_CARDS + pnic)) {
                    //if (VALID_RANGE_PAIRS[pic][pnic]) {
                    if (player_is_plyr_i) {
                        if (node.getShowdownWinner(pic, pnic)) {
                            nodeValueTemp += p_not_i[pnic];
                        } else if (!node.getShowdownDrawn(pic, pnic)) {
                            nodeValueTemp += -p_not_i[pnic];
                        }
                    } else {
                        if (node.getShowdownWinner(pnic, pic)) {
                            nodeValueTemp += -p_not_i[pnic];
                        } else if (!node.getShowdownDrawn(pnic, pic)) {
                            nodeValueTemp += p_not_i[pnic];
                        }
                    }
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

    private void update_regret_and_strategy_sum(Node node, double[] nodeValue, double[][] strategy,
                                                double[] pi, double weight) {
        for (int c = 0; c < NUM_CARDS; c++) {
            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (node.validActions[a]) {
                    double regret = node.values[a][c] - nodeValue[c];
                    // According to paper, no weighing of regret here?
//                        node.regretSum[a][c] += regret;
                    node.regretSum[a][c] = Math.max(node.regretSum[a][c] + regret, 0.0);
                    node.strategySum[a][c] += pi[c] * strategy[a][c] * weight;
                }
            }
        }
    }

    private double[] exploit(Node node, double[] pi, double[] pni, int plyr_i) {
        int player = node.player;
        int plyr_not_i = 1 - plyr_i;
        boolean player_is_plyr_i = player == plyr_i;
        double[] p_not_i = node.p[plyr_not_i];
        double winSize = node.winSize;
        double nodeValueTemp;
        int oppCount;

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
//                double opponentUnblockedSum = 0;
                nodeValueTemp = 0;
                oppCount = 0;
                for (int pnic = 0; pnic < NUM_CARDS; pnic++) {
                    if (node.validRanges.get(pic * NUM_CARDS + pnic)) {
                        //if (VALID_RANGE_PAIRS[pic][pnic]) {
                        if (player_is_plyr_i) {
                            if (node.getShowdownWinner(pic, pnic)) {
                                nodeValueTemp += p_not_i[pnic];
                            } else if (!node.getShowdownDrawn(pic, pnic)) {
                                nodeValueTemp += -p_not_i[pnic];
                            }
                        } else {
                            if (node.getShowdownWinner(pnic, pic)) {
                                nodeValueTemp += -p_not_i[pnic];
                            } else if (!node.getShowdownDrawn(pnic, pic)) {
                                nodeValueTemp += p_not_i[pnic];
                            }
                        }
//                    opponentUnblockedSum += node.p[plyr_not_i][pnic];
                        oppCount += 1;
                    }
                }
//                if (opponentUnblockedSum > 0) {
//                    nodeValue[pic] *= 1 / opponentUnblockedSum;
//                }
                if (oppCount > 0) {
                    nodeValue[pic] = winSize * nodeValueTemp / oppCount;
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
        int iterations = 100000;
        Trainer trainer = new Trainer();
        trainer.train(iterations, iterations / 10);

        for (Node n : trainer.nodeMap.values()) {
            if (n instanceof BoardNode) {
                System.out.print(n.infoSet);
                System.out.println(Arrays.deepToString(n.values));
            }
        }
        trainer.saveOutput();
    }

    public void saveOutput() {
        String[] handNames = new String[NUM_CARDS];
        TreeMap<String, String> cardIntToNameMap = Util.generateCardIntToNameMap();
        for (int i = 0; i < NUM_CARDS; i++) {
            handNames[i] = Util.handToString(RANGES[i], cardIntToNameMap);
        }

        try {
            PrintWriter writer = new PrintWriter("cfr-result.txt", "UTF-8");
            writer.println("Exploitability");
            writer.println(result[0]);
            writer.println("Player 0 value");
            writer.println(result[1]);
            writer.println("Player 1 value");
            writer.println(result[2]);
            for (Node n : nodeMap.values()) {
                if ((n.numValidActions > 1) && !(n instanceof BoardNode)) {
//                    writer.print(n.player);
//                    writer.print(n.infoSet);
//                    writer.print("      ");
//                    writer.println();
                    writer.printf("%s %-10s %-10s P0 Prob: %-10.8f P1 Prob: %-8.6f %n", n.player, n.infoSet,
                            Util.handToString(Util.arrayConcatenate(board, n.newBoardCards), cardIntToNameMap),
                            Util.arraySum(n.p[0]), Util.arraySum(n.p[1]));
                    double[][] actualStrategy = Util.transposeMatrix(n.getActualStrategy());
                    double[][] values = Util.transposeMatrix(n.values);
                    for (int i = 0; i < NUM_CARDS; i++) {
                        if (n.p[n.player][i] > 0.0001 / NUM_CARDS) {
                            writer.printf("%s %-34s %-8.6f %-34s%n", Util.handToString(RANGES[i], cardIntToNameMap),
                                    Util.arrayToString(actualStrategy[i]),
                                    n.p[n.player][i] * NUM_CARDS,
                                    Util.arrayToString(values[i]));
                        }
                    }
                }
            }
            writer.close();
        } catch (Exception e) {
            printStackTrace();
        }
    }
}