package com.azurefractal;

public class Main {

    public static void main(String[] args) {
        int iterations = 2000000;
        new Trainer().train(iterations);
        PokerCard pokerCard = PokerCard.parse("Ad");
    }
}
