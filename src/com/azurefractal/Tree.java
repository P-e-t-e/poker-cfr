package com.azurefractal;

import com.azurefractal.Node.BoardNode;
import com.azurefractal.Node.Node;
import com.azurefractal.Node.TerminalNode;

import java.util.TreeMap;

public class Tree {
    public static void buildTree(Node rootNode, TreeMap nodeMap, Trainer trainer) {
        nodeMap.put("", rootNode);

        int[] newBoardCards = new int[0];
        buildTreeFrom(rootNode, nodeMap, trainer, 0, trainer.BETS_LEFT, newBoardCards);
        System.out.print("Tree created with number of infosets:");
        System.out.println(nodeMap.size());
    }

    public static void buildTreeFrom(Node node, TreeMap nodeMap, Trainer trainer,
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
                    if (streetNumber < trainer.NUM_STREETS) {
                        for (int bc = 0; bc < trainer.NUM_BOARD_CARDS; bc++) {
                            String newInfoSet = infoSet + "." + Integer.toString(bc) + ".";
                            if (newInfoSet.length() % 2 == 1) {
                                newInfoSet += ".";
                            }
                            int newCard = trainer.DECK[bc];

                            Node newNode = addNewNode(newInfoSet, node, nodeMap, trainer, Util.arrayAppend(newBoardCards, newCard), newBetsLeft > 0);
                            node.childNodes[bc] = newNode;
                            buildTreeFrom(newNode, nodeMap, trainer,
                                    streetNumber, newBetsLeft, Util.arrayAppend(newBoardCards, newCard));
                        }
                    }
                } else if (!node.is_terminal) {
                    // Non terminal Node
                    String newInfoSet = infoSet + Trainer.ACTION_NAMES[a];
                    boolean isBoardNodeNext = checkIfBoardNodeNext(newInfoSet, streetNumber, trainer);
                    boolean isTerminalNodeNext = checkIfTerminalNodeNext(newInfoSet, streetNumber, trainer);
                    Node newNode;
                    if (isBoardNodeNext) {
                        newNode = addNewBoardNode(newInfoSet, node, nodeMap, trainer, newBoardCards, newBetsLeft > 0);
                    } else if (isTerminalNodeNext) {
//                        System.out.println(newInfoSet);
                        newNode = addNewTerminalNode(newInfoSet, node, nodeMap, trainer, newBoardCards);
                    } else {
                        newNode = addNewNode(newInfoSet, node, nodeMap, trainer, newBoardCards, newBetsLeft > 0);
                    }
                    node.childNodes[a] = newNode;
                    buildTreeFrom(newNode, nodeMap, trainer, streetNumber, newBetsLeft, newBoardCards);
                }
            }
        }
    }

    public static Node addNewNode(String infoSet, Node parent_node, TreeMap nodeMap, Trainer trainer,
                                  int[] newBoardCards, boolean can_bet) {
        boolean[] validActions = {true, can_bet, infoSet.charAt(infoSet.length() - 1) == 'b'};
        Node node = new Node(validActions, infoSet, trainer, newBoardCards);
        node.parent_node = parent_node;
        nodeMap.put(infoSet, node);
        return node;
    }

    public static Node addNewBoardNode(String infoSet, Node parent_node, TreeMap nodeMap, Trainer trainer,
                                       int[] newBoardCards, boolean can_bet) {
        boolean[] validActions = {true, can_bet, false};
        Node node = new BoardNode(validActions, infoSet, trainer, newBoardCards);
        node.parent_node = parent_node;
        nodeMap.put(infoSet, node);
        return node;
    }

    public static Node addNewTerminalNode(String infoSet, Node parent_node, TreeMap nodeMap, Trainer trainer,
                                          int[] newBoardCards) {
        boolean[] validActions = {false, false, false};
        Node node = new TerminalNode(validActions, infoSet, trainer, newBoardCards);
        node.parent_node = parent_node;
        nodeMap.put(infoSet, node);
        return node;
    }

    public static boolean checkIfBoardNodeNext(String infoSet, int street_number, Trainer trainer) {
        int lenInfoSet = infoSet.length();
        String endingString1 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 1, lenInfoSet) : "";
        String endingString2 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 2, lenInfoSet) : "";

        if (endingString1.equals("c") || endingString2.equals("pp")) {
            if (street_number + 1 < trainer.NUM_STREETS) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkIfTerminalNodeNext(String infoSet, int street_number, Trainer trainer) {
        int lenInfoSet = infoSet.length();
        String endingString1 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 1, lenInfoSet) : "";
        String endingString2 = (lenInfoSet > 1) ? infoSet.substring(lenInfoSet - 2, lenInfoSet) : "";

        if (endingString2.equals("bp")) {
            return true;
        } else if (endingString1.equals("c") || endingString2.equals("pp")) {
            if (street_number + 1 >= trainer.NUM_STREETS) {
                return true;
            }
        }
        return false;
    }
}
