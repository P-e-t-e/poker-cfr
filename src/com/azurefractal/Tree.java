package com.azurefractal;

import java.util.TreeMap;

public class Tree {

    public static void buildTree(Node rootNode, TreeMap nodeMap) {
        nodeMap.put("", rootNode);
        buildTreeFrom(rootNode, nodeMap);
    }

    public static void buildTreeFrom(Node node, TreeMap nodeMap) {
        for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
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
                    String newInfoSet = infoSet + Trainer.ACTION_NAMES[a];
                    Node newNode = addNewNode(newInfoSet, node, nodeMap);
                    buildTreeFrom(newNode, nodeMap);
                }
            }
        }
    }

    public static Node addNewNode(String infoSet, Node parent_node, TreeMap nodeMap) {
        boolean[] validActions = {true, infoSet.charAt(infoSet.length() - 1) != 'b',
                infoSet.charAt(infoSet.length() - 1) == 'b'};
        Node node = new Node(validActions, infoSet);
        node.parent_node = parent_node;
        nodeMap.put(infoSet, node);
        return node;
    }
}
