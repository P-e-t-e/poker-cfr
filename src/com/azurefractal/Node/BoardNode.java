package com.azurefractal.Node;

import com.azurefractal.Trainer;

public class BoardNode extends Node {
    public BoardNode(boolean[] validActions, String infoSet, Trainer trainer) {
        super(validActions, infoSet, trainer);
        childNodes = new Node[trainer.NUM_BOARD_CARDS];
        values = new double[trainer.NUM_BOARD_CARDS][trainer.NUM_CARDS];
    }
}