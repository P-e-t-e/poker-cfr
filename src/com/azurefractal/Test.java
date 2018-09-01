package com.azurefractal;

public class Test {

    public static void main(String[] args) {
        runKuhnPokerTest();
        runBinaryPokerTest();
    }

    private static void runKuhnPokerTest() {
        int iterations = 2000000;
        Trainer trainer = new Trainer();
        trainer.RANGES = Ranges.get_kuhn_range();
        trainer.NUM_CARDS = trainer.RANGES.length;
        trainer.VALID_RANGE_PAIRS = Util.InitializeValidRangePairs(trainer.RANGES);
        trainer.DECK = Util.generateRemainingDeck(trainer.board);
        trainer.NUM_BOARD_CARDS = 48;
        trainer.NUM_STREETS = 1;
        trainer.RELATIVE_BET_SIZE = 0.5;
        trainer.BETS_LEFT = 1;

        double[] result = trainer.train(iterations, iterations / 10);
        assert result[0] < 0.0001;
        assert Util.isClose(result[1], -0.0555, 0.0001);
        assert Util.isClose(result[2], 0.0555, 0.0001);
        System.out.println("Kuhn poker test case passed.");
    }

    private static void runBinaryPokerTest() {
        int iterations = 1000000;
        Trainer trainer = new Trainer();
        trainer.RANGES = Ranges.get_binary_range();
        trainer.NUM_CARDS = trainer.RANGES.length;
        trainer.VALID_RANGE_PAIRS = Util.InitializeValidRangePairs(trainer.RANGES);
        trainer.DECK = Util.generateRemainingDeck(trainer.board);
        trainer.NUM_BOARD_CARDS = 48;
        trainer.NUM_STREETS = 2;
        trainer.RELATIVE_BET_SIZE = 0.5;
        trainer.BETS_LEFT = 3;

        double[] result = trainer.train(iterations, iterations / 10);
        assert result[0] < 0.0001;
        assert Util.isClose(result[1], -0.00, 0.0001);
        assert Util.isClose(result[2], 0.00, 0.0001);
        System.out.println("Binary poker test case passed.");
    }
}
