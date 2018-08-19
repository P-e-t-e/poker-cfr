package com.azurefractal;

import javax.lang.model.type.NullType;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;

public class Trainer {
    private static final String[] ACTION_NAMES = {"p", "b", "c"};
    public static final int NUM_ACTIONS = 3;
    private static final int[][] RANGES = Ranges.get_kuhn_range();
    private static final int[] board = {PokerCard.to_int("2s"), PokerCard.to_int("4h"), PokerCard.to_int("6s")};
    public static final int NUM_CARDS = RANGES.length;
    private static final int RELATIVE_BET_SIZE = 1;
    private static final Random random = new Random(0);
    private TreeMap<String, Node> nodeMap = new TreeMap<String, Node>();
    private Node rootNode = new Node(new boolean[]{true, true, false}, "");
    private static final int INF = 999999;
    private static final boolean is_mccfr = true;

    public double calculateNetExploitability() {
        return 0.5 * (calculateExploitabilityFor(0) + calculateExploitabilityFor(1));
    }

    public double calculateExploitabilityFor(int exploiter) {
        assignReachProbabilities();

        double exploitative_value = 0.0;
        for (int c = 0; c < NUM_CARDS; c++) {
            double card_value = findNodeValue("", exploiter, c) / NUM_CARDS;
            exploitative_value += card_value;
//            System.out.print("Value while holding card:");
//            System.out.println(c);
//            System.out.println(card_value);
        }
//        System.out.println("Value in total:");
//        System.out.println(exploitative_value);
        return exploitative_value;
    }

    private double findNodeValue(String history, int exploiter, int card) {
        int player = history.length() % 2;

//        System.out.print("findNodeValue was called with: ");
//        System.out.println(history);

        String infoSet;
        if (player == exploiter) {
            // It's our turn. Let's exploit.
            infoSet = card + history;
            Node node = nodeMap.get(infoSet);
            if (node == null) {
                System.out.println("Null node encountered");
                System.out.println(card + history);
                return 0.0;
            }
            // Base case: terminal node. Find showdown value
            if (node.is_terminal) {
                int victim = (exploiter + 1) % 2;
//                System.out.println("Exploiter's turn");
//                System.out.println(infoSet);
//                System.out.println(Arrays.deepToString(node.reach_prob));
                double expected_value = 0.0;
                double normalization = 0.0000000000001;
                for (int victim_card = 0; victim_card < NUM_CARDS; victim_card++) {
//                    System.out.println(determineShowdownValue(history, card, victim_card));
                    int card0 = (exploiter == 0) ? card : victim_card;
                    int card1 = (exploiter == 1) ? card : victim_card;
                    // Note the plus sign due to player not being exploiter.
                    expected_value += 1 * determineShowdownValue(history, card, victim_card) * node.reach_prob[card0][card1];
                    normalization += node.reach_prob[card0][card1];
                }
                return expected_value / normalization;
            }
            // Non base case: Find the best choice and take it.
            double[] values = new double[NUM_ACTIONS];

            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (node.validActions[a]) {
                    String nextHistory = history + ACTION_NAMES[a];
                    values[a] = findNodeValue(nextHistory, exploiter, card);
                } else {
                    values[a] = -Trainer.INF;
                }
            }
//            System.out.print("Our choice values: ");
//            System.out.print(infoSet);
//            System.out.println(Arrays.toString(values));
            return Arrays.stream(values).max().orElseThrow(() -> new IllegalArgumentException("Array is empty"));
        } else {
            // It's the victim's turn. Consider all possible states the victim could be in:
            double expected_value = 0.0;
            double normalization = 0.0000000000001;
            Node testNode = nodeMap.get(card + history);
            if (testNode == null) {
                System.out.println("Null node encountered");
                System.out.println(card + history);
                return 0.0;
            }
            if (testNode.is_terminal) {
                for (int victim_card = 0; victim_card < NUM_CARDS; victim_card++) {
                    // Not sure if this check is sufficient or general
                    if (victim_card != card) {
                        infoSet = victim_card + history;
                        Node node = nodeMap.get(infoSet);
                        if (node == null) {
                            System.out.println("Null node encountered");
                            System.out.println(card + history);
                            return 0.0;
                        }
//                        System.out.print(card);
//                        System.out.print(victim_card);
//                        System.out.println("Victim's turn");
//                        System.out.println(infoSet);
//                        System.out.println(Arrays.deepToString(node.reach_prob));
//                        System.out.println(determineShowdownValue(history, victim_card, card));
                        int card0 = (exploiter == 0) ? card : victim_card;
                        int card1 = (exploiter == 1) ? card : victim_card;
                        // Note the minus sign due to player not being exploiter.
                        expected_value += -1 * determineShowdownValue(history, victim_card, card) * node.reach_prob[card0][card1];
                        normalization += node.reach_prob[card0][card1];
                    }
                }
            } else {
                double[] values = new double[NUM_ACTIONS];
                for (int victim_card = 0; victim_card < NUM_CARDS; victim_card++) {
                    infoSet = victim_card + history;
                    Node node = nodeMap.get(infoSet);
                    double[] strategy = node.getActualStrategy();

                    //Find value of a node like 0p, which is the weighted sum of 0pb, 0pp, etc.
                    for (int a = 0; a < NUM_ACTIONS; a++) {
                        String nextHistory = history + ACTION_NAMES[a];
                        if (node.validActions[a]) {
                            double value = findNodeValue(nextHistory, exploiter, card);
                            values[a] = value;

                            int card0 = (exploiter == 0) ? card : victim_card;
                            int card1 = (exploiter == 1) ? card : victim_card;
                            expected_value += values[a] * node.reach_prob[card0][card1] * strategy[a];
                            normalization += node.reach_prob[card0][card1] * strategy[a];
                        }
                    }
                }
            }
//            System.out.print("V_Call Non base return: ");
//            System.out.println(expected_value / normalization);
//            System.out.println(normalization);
            return expected_value / normalization;
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

    private void assignReachProbabilities() {
        for (Node n : nodeMap.values()) {
            n.reach_prob = new double[NUM_CARDS][NUM_CARDS];
        }
        for (int card0 = 0; card0 < NUM_CARDS; card0++) {
            for (int card1 = 0; card1 < NUM_CARDS; card1++) {
//                assignNodeReachProb("", card0, card1, 1.0 / NUM_CARDS, 1.0 / NUM_CARDS);
                if (card0 != card1) {
                    assignNodeReachProb("", card0, card1, 1.0 / (NUM_CARDS * (NUM_CARDS - 1)));
                }
            }
        }
    }

    private void assignNodeReachProb(String history, int card0, int card1, double prob) {
        // Assign the reach probability
        int player = history.length() % 2;
        // On even turns, it is plyr0's turn to act. On odd turns, it is plyr1's turn to act.
        String infoSet = (player == 0) ? card0 + history : card1 + history;
        Node node = nodeMap.get(infoSet);

        if (node == null) {
            System.out.println("Null node encountered");
            System.out.println(infoSet);
            return;
        } else {
            node.reach_prob[card0][card1] += prob;
        }

        // Base case: terminal node
        if (node.is_terminal) {
            return;
        }

        double[] strategy = node.getActualStrategy();

        for (int a = 0; a < NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                String nextHistory = history + ACTION_NAMES[a];
                if (player == 0) {
                    assignNodeReachProb(nextHistory, card0, card1, prob * strategy[a]);
                } else {
                    assignNodeReachProb(nextHistory, card0, card1, prob * strategy[a]);
                }
            }
        }

    }

    public void buildTree() {
        for (int i = 0; i < NUM_CARDS; i++) {
            Node newNode = addNewNode(Integer.toString(i), rootNode);
            buildTreeFrom(newNode);
        }
    }

    public void buildTreeFrom(Node node) {
        for (int a = 0; a < NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                String infoSet = node.infoSet;
                int lenInfoSet = infoSet.length();
                String endingString1 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 1, lenInfoSet) : "";
                String endingString2 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 2, lenInfoSet) : "";
                if (endingString1.equals("c") || endingString2.equals("bp") || endingString2.equals("pp")) {
                    // Terminal Node
                    node.is_terminal = true;
                } else {
                    // Non terminal Node
                    String newInfoSet = infoSet + ACTION_NAMES[a];
                    Node newNode = addNewNode(newInfoSet, node);
                    buildTreeFrom(newNode);
                }
            }
        }
    }

    public void train(int iterations) {
        int[] cards = java.util.stream.IntStream.rangeClosed(0, NUM_CARDS - 1).toArray();
        double value = 0;
        buildTree();
        //Repeat <iterations> times
        for (int i = 0; i < iterations; i++) {
            //Shuffle cards
            for (int c1 = cards.length - 1; c1 > 0; c1--) {
                int c2 = random.nextInt(c1 + 1);
                int tmp = cards[c1];
                cards[c1] = cards[c2];
                cards[c2] = tmp;
            }
            //Calculate value for each iteration
            if (i <= 4000000) {
                value += cfr(cards, "", 1, 1, rootNode, false);
            } else {
                value += cfr(cards, "", 1, 1, rootNode, true);
            }

            if (i % 100000 == 0 && i != 0) {
                System.out.print("Net Expl:");
                System.out.print(i);
                System.out.println("");
                System.out.println(calculateNetExploitability());
                System.out.println("Average game value: " + value / iterations);
                for (Node n : nodeMap.values()) {
                    if (!n.is_terminal) {
                        System.out.println(n);
                    }
                }
            }
        }
        System.out.println("Average game value: " + value / iterations);
        for (Node n : nodeMap.values()) {
            if (!n.is_terminal) {
                System.out.println(n);
            }
        }
    }

    //This is a recursive function that returns game value
    private double cfr(int[] cards, String history, double p0, double p1, Node parent_node, boolean using_mccfr) {
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
            System.out.println("null node");
            node = addNewNode(infoSet, parent_node);
        }

        //Kuhn poker ends if there has been ((more than 1 move) and (last move is a pass or last 2 moves are bets)). Return value of game ends.
        if (plays > 1) {
            boolean terminalPass = history.charAt(plays - 1) == 'p';
            boolean terminalCall = history.charAt(plays - 1) == 'c';
            String endingString = history.substring(plays - 2, plays);

            int[] player_hole_cards = RANGES[cards[player]];
            int[] opp_hole_cards = RANGES[cards[opponent]];

            boolean isPlayerCardHigher = HandEvaluator.evaluateHandToInt(board[0], board[1], board[2], player_hole_cards[0], player_hole_cards[1])
                    < HandEvaluator.evaluateHandToInt(board[0], board[1], board[2], opp_hole_cards[0], opp_hole_cards[1]);

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

        double nodeValue = 0;

        if (!using_mccfr) {
            nodeValue = vncfrGetValue(history, node, p0, p1, cards, player, using_mccfr);
        } else {
            nodeValue = mccfrGetValue(history, node, p0, p1, cards, player, using_mccfr);
        }

        return nodeValue;
    }

    private double vncfrGetValue(String history, Node node, double p0, double p1, int[] cards, int player, boolean using_mccfr) {
        //get strategy setting realization weight depending on which player it is now
        double[] strategy = node.getStrategy(player == 0 ? p0 : p1);
        double nodeValue = 0;

        //For each action, the value is given by the value of cfr with nextHistory, passing in the probably of taking that line
        //Expected node value is given by sum of probabilities of each action times the value of that action
        for (int a = 0; a < NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                String nextHistory = history + ACTION_NAMES[a];
                node.values[a] = player == 0
                        ? -cfr(cards, nextHistory, p0 * strategy[a], p1, node, using_mccfr)
                        : -cfr(cards, nextHistory, p0, p1 * strategy[a], node, using_mccfr);
                nodeValue += strategy[a] * node.values[a];
            }
        }

        //For each action, regret is the (value of taking action a) minus the node value
        //Add this to the regret sum weighted by probability p1 or p0
        for (int a = 0; a < NUM_ACTIONS; a++) {
            double regret = node.values[a] - nodeValue;
            node.regretSum[a] = Math.max(node.regretSum[a] + (player == 0 ? p1 : p0) * regret, 0.0);
//            node.regretSum[a] += (player == 0 ? p1 : p0) * regret;
        }

        return nodeValue;
    }

    private double mccfrGetValue(String history, Node node, double p0, double p1, int[] cards, int player, boolean using_mccfr) {
        double[] strategy = node.getStrategy(player == 0 ? p0 : p1);
        double[] probabilities = strategy;
        double nodeValue = 0;

        //Choose a random action
        int chosenAction = 0;
        if (random.nextDouble() < 0.0) {
            int randomInt = random.nextInt(NUM_ACTIONS);
            while (!node.validActions[randomInt]) {
                randomInt = random.nextInt(NUM_ACTIONS);
            }
            assert (node.validActions[chosenAction]);
        } else {
            double randomDouble = random.nextDouble();
            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (randomDouble < (probabilities[a])) {
                    chosenAction = a;
                } else {
                    randomDouble -= (probabilities[a]);
                }
            }
        }

//        System.out.println(Arrays.toString(strategy));
//        System.out.println(chosenAction);

        //For chosen action, recompute values.
        String nextHistory = history + ACTION_NAMES[chosenAction];
        node.values[chosenAction] = player == 0
                ? -cfr(cards, nextHistory, p0 * strategy[chosenAction], p1, node, using_mccfr)
                : -cfr(cards, nextHistory, p0, p1 * strategy[chosenAction], node, using_mccfr);

        //Then resum over action values.
        for (int a = 0; a < NUM_ACTIONS; a++) {
            nodeValue += strategy[a] * node.values[a];
        }

        //For each action, regret is the (value of taking action a) minus the node value
        //Add this to the regret sum weighted by probability p1 or p0
        for (int a = 0; a < NUM_ACTIONS; a++) {
            double regret = node.values[a] - nodeValue;
            node.regretSum[a] = Math.max(node.regretSum[a] + (player == 0 ? p1 : p0) * regret, 0.0);
        }

        return nodeValue;
    }

    private Node addNewNode(String infoSet, Node parent_node) {
        boolean[] validActions = {true, infoSet.charAt(infoSet.length() - 1) != 'b',
                infoSet.charAt(infoSet.length() - 1) == 'b'};
        Node node = new Node(validActions, infoSet);
        node.child_node = parent_node;
        nodeMap.put(infoSet, node);
        return node;
    }

    public static void main(String[] args) {
        int iterations = 1000000;
        Trainer trainer = new Trainer();
        trainer.train(iterations);
        System.out.println("Net Expl:");
        System.out.println(trainer.calculateNetExploitability());
//        for (Node n : trainer.nodeMap.values()) {
//            System.out.print(n.infoSet);
//            System.out.println(Arrays.deepToString(n.reach_prob));
//        }
//        System.out.println(trainer.determineShowdownValue("bc", 0, 1));
//        System.out.println(trainer.determineShowdownValue("bc", 2, 1));
    }

}