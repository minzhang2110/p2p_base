package com.zm.utils;

import java.math.BigInteger;

/**
 * Created by zhangmin on 2015/9/22.
 */
public class ParseUtils {
    public static byte parseByte(String value){
        short ret;
        try {
            ret = Short.parseShort(value);
        }catch (Exception e){
            throw new IllegalArgumentException("数值格式错误：" + value);
        }
        if(ret > BYTEMAX || ret < BYTEMIN)
            throw new IllegalArgumentException(ret + " 参数越界，范围应是" + BYTEMIN+ " ---- " + BYTEMAX);
        return  (byte) ret;
    }

    public static short parseShort(String value){
        int ret;
        try {
            ret = Integer.parseInt(value);
        }catch (Exception e){
            throw new IllegalArgumentException("数值格式错误：" + value);
        }
        if(ret > SHORTMAX || ret < SHORTMIN)
            throw new IllegalArgumentException(ret + " 参数越界，范围应是" + SHORTMIN+ " ---- " + SHORTMAX);
        return (short) ret;
    }

    public static int parseInt(String value){
        long ret;
        try {
            ret = Long.parseLong(value);
        }catch (Exception e){
            throw new IllegalArgumentException("数值格式错误：" + value);
        }
        if(ret > INTMAX || ret < INTMIN)
            throw new IllegalArgumentException(ret + " 参数越界，范围应是" + INTMIN+ " ---- " + INTMAX);
        return (int) ret;
    }

    public static long parseLong(String value){
        BigInteger ret;
        try {
            ret = new BigInteger(value);
        }catch (Exception e){
            throw new IllegalArgumentException("数值格式错误：" + value);
        }
        if(ret.compareTo(LONGMAX) > 0 || ret.compareTo(LONGMIN) < 0)
            throw new IllegalArgumentException(ret + " 参数越界，范围应是" + LONGMIN+ " ---- " + LONGMAX);
        return ret.longValue();
    }
    // 字节数值范围：-128~255，转换成byte类型为-128~127
    static final short BYTEMAX = Byte.MAX_VALUE - Byte.MIN_VALUE;
    static final short BYTEMIN =  Byte.MIN_VALUE;

    //双字节数值范围：-32768~65535，转换成short类型为-32768~32767
    static final int SHORTMAX = Short.MAX_VALUE - Short.MIN_VALUE;
    static final int SHORTMIN =  Short.MIN_VALUE;

    //四字节数值范围：-2147483648~4294967295，转换成int类型为-2147483648~2147483647
    static final long INTMAX =(long) Integer.MAX_VALUE - Integer.MIN_VALUE;
    static final long INTMIN =(long) Integer.MIN_VALUE;

    //八字节数值范围：-9223372036854775808~18446744073709551615
    //转换成long类型为-9223372036854775808~9223372036854775807
    static final BigInteger LONGMAX = new BigInteger("" + Long.MAX_VALUE).subtract(new BigInteger("" + Long.MIN_VALUE));
    static final BigInteger LONGMIN = new BigInteger("" + Long.MIN_VALUE);

    public static void main(String[] args){
        System.out.println(parseByte("-128"));
        System.out.println(parseByte("0"));
        System.out.println(parseByte("127"));
        System.out.println(parseByte("255"));

        System.out.println(parseShort("-32768"));
        System.out.println(parseShort("0"));
        System.out.println(parseShort("32767"));
        System.out.println(parseShort("65535"));

        System.out.println(parseInt("-2147483648"));
        System.out.println(parseInt("0"));
        System.out.println(parseInt("2147483647"));
        System.out.println(parseInt("4294967295"));

        System.out.println(parseLong("-9223372036854775808"));
        System.out.println(parseLong("0"));
        System.out.println(parseLong("9223372036854775807"));
        System.out.println(parseLong("18446744073709551615"));
    }
}
