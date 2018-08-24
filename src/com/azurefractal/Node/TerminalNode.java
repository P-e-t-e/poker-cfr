package com.azurefractal.Node;

import com.azurefractal.Trainer;

public class TerminalNode extends Node {
    public TerminalNode(boolean[] validActions, String infoSet) {
        super(validActions, infoSet);
        showdownValue = new double[Trainer.NUM_CARDS][Trainer.NUM_CARDS];
        calculateShowdownValue();
    }

    public void calculateShowdownValue() {
        for (int pc = 0; pc < Trainer.NUM_CARDS; pc++) {
            for (int oc = 0; oc < Trainer.NUM_CARDS; oc++) {
                if (pc != oc) {
                    showdownValue[pc][oc] = determineShowdownValue(pc, oc);
                }
            }
        }
    }
}