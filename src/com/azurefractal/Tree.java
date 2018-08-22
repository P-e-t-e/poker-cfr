package com.azurefractal;

import java.util.TreeMap;

public class Tree {

    public static void buildTree(Node rootNode, TreeMap nodeMap) {
        nodeMap.put("", rootNode);
        TreeMap<String, String> rules = new TreeMap<>();
        rules.put("n_streets", "3");
        buildTreeFrom(rootNode, nodeMap, rules, 0);
        System.out.print("Tree created with number of infosets:");
        System.out.println(nodeMap.size());
    }

    public static void buildTreeFrom(Node node, TreeMap nodeMap, TreeMap<String, String> rules, int street_number) {
        for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
            if (node.validActions[a]) {
                String infoSet = node.infoSet;
                int lenInfoSet = infoSet.length();
                String endingString1 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 1, lenInfoSet) : "";
                String endingString2 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 2, lenInfoSet) : "";
                if (endingString2.equals("bp")) {
                    // Terminal Node
                    node.is_terminal = true;
                } else {
                    if (endingString1.equals("c") || endingString2.equals("pp")) {
                        street_number += 1;
                        if (street_number >= Integer.parseInt(rules.get("n_streets"))) {
                            node.is_terminal = true;
                        } else {
                            node.is_boardnode = true;
                            for (int bc = 0; bc < Trainer.NUM_BOARD_CARDS; bc++) {
                                String newInfoSet = infoSet + "." + Integer.toString(bc) + ".";
                                if (newInfoSet.length() % 2 == 1) {
                                    newInfoSet += ".";
                                }
                                Node newNode = addNewNode(newInfoSet, node, nodeMap, street_number);
                                node.childNodes[bc] = newNode;
                                buildTreeFrom(newNode, nodeMap, rules, street_number);
                            }
                        }
                    } else {
                        // Non terminal Node
                        String newInfoSet = infoSet + Trainer.ACTION_NAMES[a];
                        boolean isBoardNodeNext = checkIfBoardNodeNext(newInfoSet, street_number, rules);
                        Node newNode;
                        if (isBoardNodeNext) {
                            newNode = addNewBoardNode(newInfoSet, node, nodeMap, street_number);
                        } else {
                            newNode = addNewNode(newInfoSet, node, nodeMap, street_number);
                        }
                        node.childNodes[a] = newNode;
                        buildTreeFrom(newNode, nodeMap, rules, street_number);
                    }
                }
            }
        }
    }

    public static Node addNewNode(String infoSet, Node parent_node, TreeMap nodeMap, int street_number) {
        boolean[] validActions = {true, infoSet.charAt(infoSet.length() - 1) != 'b',
                infoSet.charAt(infoSet.length() - 1) == 'b'};
        Node node = new Node(validActions, infoSet, street_number);
        node.parent_node = parent_node;
        nodeMap.put(infoSet, node);
        return node;
    }

    public static Node addNewBoardNode(String infoSet, Node parent_node, TreeMap nodeMap, int street_number) {
        boolean[] validActions = {true, true, false};
        Node node = new BoardNode(validActions, infoSet, street_number);
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
}
