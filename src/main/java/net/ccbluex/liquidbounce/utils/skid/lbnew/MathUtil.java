package net.ccbluex.liquidbounce.utils.skid.lbnew;

import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDLayoutAttributeObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Random;

/* loaded from: quickmarcobounce-v0.0.3(Scaffold2更新1).jar:net/ccbluex/liquidbounce/utils/math/MathUtil.class */
public class MathUtil {
    public static final DecimalFormat DF_0 = new DecimalFormat(PDLayoutAttributeObject.GLYPH_ORIENTATION_VERTICAL_ZERO_DEGREES);
    public static final DecimalFormat DF_1 = new DecimalFormat("0.0");
    public static final DecimalFormat DF_2 = new DecimalFormat("0.00");
    public static final DecimalFormat DF_1D = new DecimalFormat("0.#");
    public static final DecimalFormat DF_2D = new DecimalFormat("0.##");
    public static final SecureRandom secureRandom = new SecureRandom();

    public static int getRandomInRange(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }



    public static float getRandomInRange(float min, float max) {
        SecureRandom random = new SecureRandom();
        return (random.nextFloat() * (max - min)) + min;
    }

    public static double getRandomInRange(double min, double max) {
        SecureRandom random = new SecureRandom();
        return min == max ? min : (random.nextDouble() * (max - min)) + min;
    }

    public static int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static double lerp(double old, double newVal, double amount) {
        return ((1.0d - amount) * old) + (amount * newVal);
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return Double.valueOf(oldValue + ((newValue - oldValue) * interpolationValue));
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).floatValue();
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double output = 1.0d / Math.sqrt(6.283185307179586d * (sigma * sigma));
        return (float) (output * Math.exp((-(x * x)) / (2.0d * (sigma * sigma))));
    }

    public static double roundToHalf(double d) {
        return Math.round(d * 2.0d) / 2.0d;
    }

    public static double round(double num, double increment) {
        BigDecimal bd = new BigDecimal(num);
        return bd.setScale((int) increment, RoundingMode.HALF_UP).doubleValue();
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        return bd.setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    public static String round(String value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        return bd.stripTrailingZeros().setScale(places, RoundingMode.HALF_UP).toString();
    }

    public static float getRandomFloat(float max, float min) {
        SecureRandom random = new SecureRandom();
        return (random.nextFloat() * (max - min)) + min;
    }

    public static int getNumberOfDecimalPlace(double value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return Math.max(0, bigDecimal.stripTrailingZeros().scale());
    }

    public static int clamp(int num, int min, int max) {
        if (num < min) {
            return min;
        }
        return Math.min(num, max);
    }

    public static double clamp(double num, double min, double max) {
        if (num < min) {
            return min;
        }
        return Math.min(num, max);
    }

    public static float roundToFloat(double d) {
        return (float) (Math.round(d * 1.0E8d) / 1.0E8d);
    }
}