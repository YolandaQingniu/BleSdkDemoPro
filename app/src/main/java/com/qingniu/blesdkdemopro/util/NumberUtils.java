package com.qingniu.blesdkdemopro.util;

import android.annotation.SuppressLint;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Random;

/**
 * 数值工具，包括数值的格式化，取精度等
 *
 * @author hdr
 */
public class NumberUtils {
    public final static double hUnit = 2.5372; // 遵循四舍五入

    private static final DecimalFormat COMMON_FORMATER = new DecimalFormat("0.0");
    private static final DecimalFormat COMMON_FORMAT2 = new DecimalFormat("0.00");

    /**
     * 把小数转为只有一位小数的输出
     *
     * @param value
     * @return
     */
    public static String format(float value) {
        //这种方案会导致，小数点在有些类型的语言下为逗号
//        return String.format("%.1f",value);
//        return COMMON_FORMATER.format(value);

        String v = value + "";
        return v.substring(0, v.indexOf(".") + 2);
        //这种方案是有舍入的
//        return new BigDecimal(String.valueOf(f)).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue() + "";
    }


    /**
     * @param f
     * @param scale 保留的位数
     * @return
     */
    public static float getPrecision(float f, int scale) {
        return new BigDecimal(String.valueOf(f)).setScale(scale, BigDecimal.ROUND_HALF_UP).floatValue();

/*        if (scale < 0) {
            throw new IllegalArgumentException("需要保留的位数不能小于零");
        }

        BigDecimal bigDecimalOne = new BigDecimal(Float.toString(f));
        BigDecimal bigDecimalTwo = new BigDecimal("1");

        return bigDecimalOne.divide(bigDecimalTwo, scale,
                BigDecimal.ROUND_HALF_UP).floatValue();*/

    }

    /**
     * 格式化整数到2位，不足的补0
     *
     * @param value
     * @return
     */
    public static String formatIntegerTo2(int value) {
        if (value < 10) {
            return "0" + value;
        } else
            return String.valueOf(value);
    }

    public static String format2(float value) {
        String v = value + "";
        int pos = v.indexOf(".");
        if (v.length() == pos + 2) {//只有一位小数
            return v + "0";
        } else return v.substring(0, pos + 3);
    }

    /**
     * 生成验证码
     *
     * @return
     */
    public static String getRandomNumberString() {
        StringBuilder builder = new StringBuilder();
        Random ran = new Random();
        for (int i = 0; i < 4; i++) {
            builder.append(Math.abs(ran.nextInt() % 10));
        }
        return builder.toString();

    }

    /**
     * 把所有包含空格的字符串都给替换掉
     *
     * @param resource
     * @param ch
     * @return
     */
    public static String removeAllSpace(String resource, char ch) {
        StringBuffer buffer = new StringBuffer();
        int position = 0;
        char currentChar;
        while (position < resource.length()) {
            currentChar = resource.charAt(position++);
            if (currentChar != ch) {
                buffer.append(currentChar);
            }
        }
        return buffer.toString();
    }

    public static String[] splitString(float score) {
        String[] scoreInfo = (score + "").split("\\.");
        scoreInfo[1] = "." + scoreInfo[1] + "分";
        return scoreInfo;

    }

    public static String[] splitShape(String info) {
        String[] infos = (info + "").split("\\-");
        return infos;
    }

    public static String[] splitHealth(String info) {
        String[] infos = info.split("\\" + info.substring(1, 2) + "");
        return infos;

    }

    public static char[] getInput() {
        char[] c = new char[70];
        int[] a = new int[26];
        for (int i = 0; i < a.length; i++) {
            a[i] = 'a' + i;
            c[i] = (char) a[i];
        }

        char[] d = new char[26];
        int[] b = new int[26];
        for (int i = 0; i < b.length; i++) {
            b[i] = 'A' + i;
            d[i] = (char) b[i];
        }

        System.arraycopy(d, 0, c, 26, d.length);

        char[] number = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '@', '.', '-', '_', ','};

        System.arraycopy(number, 0, c, 52, number.length);
        return c;

    }

    /**
     * 千克转英石，如果<14磅，就显示磅，>=14磅，显示英石
     *
     * @return
     */
    public static float kgToStValue(float kg) {
        return getOnePrecision(kgToLb(kg) / 14f);
    }

    /**
     * 四舍五入
     *
     * @param f
     * @return
     */
    public static float getOnePrecision(float f) {
        return getOnePrecision((double) f);
    }

    /**
     * 千克转磅
     * lb = ((((kg*100) * 11023 + 50000)/100000)<<1) / 10
     */
    public static float kgToLb(float kg) {
        float temp = kg * 100;
        return getOnePrecision(((int) ((temp * 11023 + 50000) / 100000) << 1) / 10f);
    }

    public static float getOnePrecision(double f) {
        float value;
        try {
            value = new BigDecimal(String.valueOf(f)).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        } catch (NumberFormatException e) {
            value = 0;
        }
        return value;
    }

    /**
     * 千克转英石,两位小数，如果<14磅，就显示磅，>=14磅，显示英石
     *
     * @return
     */
    public static float kgToStValueTwoPrecision(float kg) {
        return getTwoPrecision(kgToLb(kg) / 14f);
    }

    public static float getTwoPrecision(double d) {
        return new BigDecimal(String.valueOf(d)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public static float lBToStFloat(float lb) {
        return lb / 14f;

    }

    public static float kgToSt(float kg) {
        float lb = kgToLb(kg);

        return Float.parseFloat(NumberUtils.getPrecisionShow(lb / 14f, 2));

    }

    /**
     * 由于四舍五入的数据，如果获取的数据为58.000，它的需要显示小数点的位数为2为或者3位；
     * 而double类型的数据，会自动将无效的0去掉，获取的值一直是58.0
     * 所以返回的数据只能是字符串
     */
    public static String getPrecisionShow(double d, int scale) {
        d = getFormat(d, scale);
        return String.format(Locale.CHINA, "%." + scale + "f", d).replace(",", ".");
    }

    /**
     * 获取double类型数据指定有效值后，四舍五入
     *
     * @param f     需要格式化的值
     * @param scale 需要格式化的小数点之后的位数
     */
    @SuppressLint("DefaultLocale")
    public static double getFormat(double f, int scale) {
        double value;
        try {
            value = new BigDecimal(String.valueOf(f)).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (NumberFormatException e) {
            value = 0;
        }
        return value;
    }

    /**
     * 千克转磅,不进行四舍五入
     */
    public static float kgToLbOther(float kg) {
        return (kg * 11023 / 10000) * 2;
    }

    /**
     * 英石转kg
     */
    public static double stToKg(double st) {
        return lbToKg(st * 14);
    }

    /**
     * 磅转成kg
     */
    public static double lbToKg(double lb) {
        return getOnePrecision(lb * 10000 / 11023 / 2);
    }

    /**
     * 英尺 转成厘米
     */
    public static float inchToCm(float in) {
        return getOnePrecision(in * hUnit);
    }

    /**
     * 将 ""'""转成cm
     */
    public static float ftToCm(int h, int in) {
        return getTwoPrecision((h * 12 + in) * hUnit);
    }

    /**
     * 将cm转换成多少英尺多少英寸
     */
    public static String cmToFtStr(double cm) {
        String[] values = NumberUtils.cmToFt(cm);
        return values[0] + "'" + values[1] + "\"";
    }

    /**
     * 将 cm转成""'""
     */
    public static String[] cmToFt(double cm) {
        String[] values = new String[2];
        float in = cmToInch((float) cm);
        values[0] = String.valueOf((int) getOnePrecision(in / 12));
        values[1] = String.valueOf(getOnePrecision(in - Double.parseDouble(values[0]) * 12));
        return values;
    }

    /**
     * 厘米 转成 英尺
     */
    public static float cmToInch(float cm) {
        return getTwoPrecision(cm / hUnit);
    }

    /**
     * 将 英寸转成""'""
     */
    public static String ftTo(int in) {
        return in / 12 + "'" + in % 12 + "\"";
    }

    /**
     * 将英寸转成 ""'"",输出数组
     */
    public static int[] inchToFt(int in) {
        int[] values = new int[2];
        values[0] = in / 12;
        values[1] = in % 12;
        return values;
    }

    /**
     * 向下取整
     */
    public static int getDownInt(double d) {
        return (int) Math.floor(d);
    }

    /**
     * 向上取整
     */
    public static int getUpInt(double d) {
        return (int) Math.ceil(d);
    }

    /**
     * 四舍五入取整
     */
    public static int getRoundInt(double d) {
        return (int) Math.round(d);
    }

    /**
     * 根据指定的范围生成随机值【-range,range)
     *
     * @param range 指定范围
     */
    public static int getRangeRandom(int range) {
        Random random = new Random();
        return random.nextInt(range * 2) - range;
    }
}
