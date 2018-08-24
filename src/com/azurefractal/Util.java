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

    public static double arraySum(double[] vector) {
        double result = 0;
        for (int i = 0; i < vector.length; i++) {
            result += vector[i];
        }
        return result;
    }

    public static boolean checkCardBlock(int[] player_cards, int[] opp_cards) {
        for (int pc = 0; pc < 2; pc++) {
            for (int oc = 0; oc < 2; oc++) {
                if (player_cards[pc] == opp_cards[oc]) {
//                    System.out.print(Arrays.toString(player_cards));
//                    System.out.println(Arrays.toString(opp_cards));
                    return true;
                }
            }
        }
        return false;
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
