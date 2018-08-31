package com.azurefractal.Node;

import com.azurefractal.Trainer;

public class BoardNode extends Node {
    public BoardNode(boolean[] validActions, String infoSet) {
        super(validActions, infoSet);
        childNodes = new Node[Trainer.NUM_BOARD_CARDS];
        values = new double[Trainer.NUM_BOARD_CARDS][Trainer.NUM_CARDS];
    }
}