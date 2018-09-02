package com.azurefractal;

import com.azurefractal.Evaluator.PokerCard;

import java.util.TreeMap;

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

        System.arraycopy(vector1, 0, result, 0, vector1.length);
        System.arraycopy(vector2, 0, result, vector1.length, vector2.length);
        return result;
    }

    public static int[] arrayConcatenate(int[] vector1, int[] vector2, int[] vector3) {
        int[] result = new int[vector1.length + vector2.length + vector3.length];

        System.arraycopy(vector1, 0, result, 0, vector1.length);
        System.arraycopy(vector2, 0, result, vector1.length, vector2.length);
        System.arraycopy(vector3, 0, result, vector1.length + vector2.length, vector3.length);
        return result;
    }

    public static double arraySum(double[] vector) {
        double result = 0;
        for (double aVector : vector) {
            result += aVector;
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

    public static boolean checkCardNotBlocked(int[] cards0, int[] cards1, int[] cards2) {
        for (int aCards0 : cards0) {
            for (int aCards1 : cards1) {
                if (aCards0 == aCards1) {
                    return false;
                }
            }
            for (int aCards2 : cards2) {
                if (aCards0 == aCards2) {
                    return false;
                }
            }
        }
        for (int aCards1 : cards1) {
            for (int aCards2 : cards2) {
                if (aCards1 == aCards2) {
                    return false;
                }
            }
        }

        return true;
//        int[] array = Util.arrayConcatenate(cards0, cards1, cards2);
//        Set<Integer> set = new HashSet<>();
//        for (int i:array) {
//            set.add(i);
//        }
////        Set<Integer> set = IntStream.of(array).boxed().collect(Collectors.toSet());
//
//        return array.length == set.size();
    }

    public static boolean[][] InitializeValidRangePairs(int[][] ranges) {
        boolean[][] result = new boolean[ranges.length][ranges.length];
        for (int pc = 0; pc < ranges.length; pc++) {
            for (int oc = 0; oc < ranges.length; oc++) {
                result[pc][oc] = checkCardNotBlocked(ranges[pc], ranges[oc]);
            }
        }
        return result;
    }

    public static boolean isClose(double value, double target, double threshold) {
        return (value > target - threshold) && (value < target + threshold);
    }

    public static TreeMap<String, String> generateCardIntToNameMap() {
        String[] suits = {"c", "d", "h", "s"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};
        TreeMap<String, String> treeMap = new TreeMap<>();

        for (int s = 0; s < 4; s++) {
            for (int r = 2; r < 15; r++) {
                int new_card = PokerCard.to_int(ranks[14 - r] + suits[s]);
                treeMap.put(Integer.toString(new_card), ranks[14 - r] + suits[s]);
            }
        }
        return treeMap;
    }

    public static String handToString(int[] hand, TreeMap<String, String> cardIntToNameMap) {
        String outputString = "";
        for (int card : hand) {
            outputString += cardIntToNameMap.get(Integer.toString(card));
        }
        return outputString;
    }

    public static String arrayToString(double[] array) {
        StringBuilder outputString = new StringBuilder("[");
        for (double value : array) {
            outputString.append(String.format("%.6f", value));
            outputString.append(", ");
        }
        outputString.append("]");
        return outputString.toString();
    }

    public static double[][] transposeMatrix(double[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;

        double[][] transposedMatrix = new double[n][m];

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                transposedMatrix[x][y] = matrix[y][x];
            }
        }

        return transposedMatrix;
    }
}
