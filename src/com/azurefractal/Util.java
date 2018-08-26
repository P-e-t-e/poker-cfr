package com.azurefractal;

import java.util.Arrays;

public class Util {
    public static double[] arrayAdd(double[] vector1, double[] vector2) {
        double[] result = new double[vector2.length];
        for (int i = 0; i < vector2.length; i++) {
            result[i] = vector1[i] + vector2[i];
        }
        return result;
    }

    public static double[] arrayMultC(double scalar, double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = scalar * vector[i];
        }
        return result;
    }

    public static double[] arrayNegate(double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = -vector[i];
        }
        return result;
    }

    public static double[] arrayDot(double[] vector1, double[] vector2) {
        double[] result = new double[vector1.length];
        for (int i = 0; i < vector1.length; i++) {
            result[i] = vector1[i] * vector2[i];
        }
        return result;
    }

    public static double[] arrayFull(double scalar, int length) {
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
            result[i] = scalar;
        }
        return result;
    }

    public static int[] arrayAppend(int[] vector1, int scalar) {
        int[] result = new int[vector1.length + 1];
        for (int i = 0; i < vector1.length; i++) {
            result[i] = vector1[i];
        }
        for (int i = vector1.length; i < result.length; i++) {
            result[i] = scalar;
        }
        return result;
    }

    public static int[] arrayConcatenate(int[] vector1, int[] vector2) {
        int[] result = new int[vector1.length + vector2.length];
        for (int i = 0; i < vector1.length; i++) {
            result[i] = vector1[i];
        }
        for (int i = vector1.length; i < result.length; i++) {
            result[i] = vector2[i - vector1.length];
        }
        return result;
    }

    public static double arraySum(double[] vector) {
        double result = 0;
        for (int i = 0; i < vector.length; i++) {
            result += vector[i];
        }
        return result;
    }

    public static boolean checkCardNotBlocked(int[] cards0, int[] cards1) {
        for (int pc = 0; pc < cards0.length; pc++) {
            for (int oc = 0; oc < cards1.length; oc++) {
                if (cards0[pc] == cards1[oc]) {
//                    System.out.print(Arrays.toString(cards0));
//                    System.out.println(Arrays.toString(cards1));
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean[][] InitializeValidRangePairs(int[][] ranges) {
        boolean[][] result = new boolean[Trainer.NUM_CARDS][Trainer.NUM_CARDS];
        for (int pc = 0; pc < Trainer.NUM_CARDS; pc++) {
            for (int oc = 0; oc < Trainer.NUM_CARDS; oc++) {
                result[pc][oc] = checkCardNotBlocked(ranges[pc], ranges[oc]);
            }
        }
        return result;
    }

    public long intentional_slow_step() {
        long nanoTime = System.nanoTime();
        long dd = 0;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                for (int k = 0; k < 100; k++) {
                    for (int l = 0; l < 10; l++) {
                        dd = Math.round(0.0001 * Math.tanh(i * j * k * l));
                    }
                }
            }
        }
        System.out.println(System.nanoTime() - nanoTime);
        return dd;
    }
}
