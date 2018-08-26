package com.azurefractal;

import com.azurefractal.Node.*;

import java.util.List;
import java.util.TreeMap;

public class Tree {

    public static void buildTree(Node rootNode, TreeMap nodeMap) {
        nodeMap.put("", rootNode);
        TreeMap<String, String> rules = new TreeMap<>();
        rules.put("n_streets", "2");
        rules.put("eff_stack", "500");
        rules.put("pot_size", "40");
        rules.put("relative_bet_size", "1.0");

        int[] newBoardCards = new int[0];
        buildTreeFrom(rootNode, nodeMap, rules, 0, Trainer.BETS_LEFT, newBoardCards);
        System.out.print("Tree created with number of infosets:");
        System.out.println(nodeMap.size());
    }

    public static void buildTreeFrom(Node node, TreeMap nodeMap, TreeMap<String, String> rules,
                                     int streetNumber, int bets_left, int[] newBoardCards) {
        for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                String infoSet = node.infoSet;
                int lenInfoSet = infoSet.length();
                String endingString1 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 1, lenInfoSet) : "";
                String endingString2 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 2, lenInfoSet) : "";
                int newBetsLeft = (a == 1) ? bets_left - 1 : bets_left;

                if (endingString1.equals("c") || endingString2.equals("pp")) {
                    streetNumber += 1;
                    if (streetNumber < Integer.parseInt(rules.get("n_streets"))) {
                        for (int bc = 0; bc < Trainer.NUM_BOARD_CARDS; bc++) {
                            String newInfoSet = infoSet + "." + Integer.toString(bc) + ".";
                            if (newInfoSet.length() % 2 == 1) {
                                newInfoSet += ".";
                            }
                            Node newNode = addNewNode(newInfoSet, node, nodeMap, newBetsLeft > 0);
                            node.childNodes[bc] = newNode;
                            buildTreeFrom(newNode, nodeMap, rules,
                                    streetNumber, newBetsLeft, Util.arrayAppend(newBoardCards, bc));
                        }
                    }
                } else if (!node.is_terminal) {
                    // Non terminal Node
                    String newInfoSet = infoSet + Trainer.ACTION_NAMES[a];
                    boolean isBoardNodeNext = checkIfBoardNodeNext(newInfoSet, streetNumber, rules);
                    boolean isTerminalNodeNext = checkIfTerminalNodeNext(newInfoSet, streetNumber, rules);
                    Node newNode;
                    if (isBoardNodeNext) {
                        newNode = addNewBoardNode(newInfoSet, node, nodeMap, newBetsLeft > 0);
                    } else if (isTerminalNodeNext) {
//                        System.out.println(newInfoSet);
                        newNode = addNewTerminalNode(newInfoSet, node, nodeMap, newBoardCards);
                    } else {
                        newNode = addNewNode(newInfoSet, node, nodeMap, newBetsLeft > 0);
                    }
                    node.childNodes[a] = newNode;
                    buildTreeFrom(newNode, nodeMap, rules, streetNumber, newBetsLeft, newBoardCards);
                }
            }
        }
    }

    public static Node addNewNode(String infoSet, Node parent_node, TreeMap nodeMap, boolean can_bet) {
        boolean[] validActions = {true, can_bet, infoSet.charAt(infoSet.length() - 1) == 'b'};
        Node node = new Node(validActions, infoSet);
        node.parent_node = parent_node;
        nodeMap.put(infoSet, node);
        return node;
    }

    public static Node addNewBoardNode(String infoSet, Node parent_node, TreeMap nodeMap, boolean can_bet) {
        boolean[] validActions = {true, can_bet, false};
        Node node = new BoardNode(validActions, infoSet);
        node.parent_node = parent_node;
        nodeMap.put(infoSet, node);
        return node;
    }

    public static Node addNewTerminalNode(String infoSet, Node parent_node, TreeMap nodeMap, int[] newBoardCards) {
        boolean[] validActions = {false, false, false};
        Node node = new TerminalNode(validActions, infoSet, newBoardCards);
        node.parent_node = parent_node;
        nodeMap.put(infoSet, node);
        return node;
    }

    public static boolean checkIfBoardNodeNext(String infoSet, int street_number, TreeMap<String, String> rules) {
        int lenInfoSet = infoSet.length();
        String endingString1 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 1, lenInfoSet) : "";
        String endingString2 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 2, lenInfoSet) : "";

        if (endingString1.equals("c") || endingString2.equals("pp")) {
            if (street_number + 1 < Integer.parseInt(rules.get("n_streets"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkIfTerminalNodeNext(String infoSet, int street_number, TreeMap<String, String> rules) {
        int lenInfoSet = infoSet.length();
        String endingString1 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 1, lenInfoSet) : "";
        String endingString2 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 2, lenInfoSet) : "";

        if (endingString2.equals("bp")) {
            return true;
        } else if (endingString1.equals("c") || endingString2.equals("pp")) {
            if (street_number + 1 >= Integer.parseInt(rules.get("n_streets"))) {
                return true;
            }
        }
        return false;
    }
}
