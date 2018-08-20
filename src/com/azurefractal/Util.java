package com.azurefractal;

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
}
