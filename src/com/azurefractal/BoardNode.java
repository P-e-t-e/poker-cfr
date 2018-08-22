package com.azurefractal;

public class BoardNode extends Node {

    BoardNode(boolean[] validActions, String infoSet, int street_number) {
        super(validActions, infoSet, street_number);
        childNodes = new Node[Trainer.NUM_BOARD_CARDS];
        is_boardnode = false;
    }
}