package com.zm.utils;

import com.zm.Field.*;
import com.zm.encryption.Encrypt;
import com.zm.message.BufferMgr;
import com.zm.message.MsgConfig;
import com.zm.message.RequestMessage;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangmin on 2015/9/22.
 */
public class Utils {
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

    //有待优化
    public static MsgConfig getMsgConfig(String p2pStr){
        String tmp = getConfigSec(p2pStr);
        MsgConfig config = new MsgConfig();
        if(tmp == null || tmp.equals(""))
            return config;//默认情况
        else{
            String[] token = tmp.split("\n");
            for(int i = 0; i<token.length; i++){
                String[] configLine = token[i].split("=");
                if(configLine[0].trim().toLowerCase().equals("order")){
                    if(configLine[1].trim().toLowerCase().equals("net"))
                        config.order = Order.NET;
                    else if(configLine[1].trim().toLowerCase().equals("host"))
                        config.order = Order.HOST;
                    else
                        throw new IllegalStateException("错误的配置值:" + configLine[1].trim());
                }else if(configLine[0].trim().toLowerCase().equals("encrypt")){
                    if(configLine[1].trim().toLowerCase().equals("none"))
                        config.encrypt = Encrypt.NONE;
                    else if(configLine[1].trim().toLowerCase().equals("aes"))
                        config.encrypt = Encrypt.AES;
                    else if(configLine[1].trim().toLowerCase().equals("mhxy"))
                        config.encrypt = Encrypt.MHXY;
                    else
                        throw new IllegalStateException("错误的配置值:" + configLine[1].trim());
                }else
                    throw new IllegalStateException("错误的配置项:" + configLine[0].trim());
            }
        }
        return config;
    }

    public static ArrayList<Field> getFieldList(String secValue, MsgConfig config){
        ArrayList<Field> fields = new ArrayList<Field>();
        String[] strFields = secValue.split("\n");
        for(int i = 0; i < strFields.length; i++){
            if(strFields[i].trim().equals(""))
                continue;
            fields.add(strToField(strFields[i], config));
        }
        return fields;
    }

    private static Field strToField(String str, MsgConfig config){
        String tmp = str.trim().replaceAll("\\s", "");
        String hostOrder = "";
        String type = "";
        String name = "";
        String value = "";
        Pattern pattern = Pattern.compile("^([\\^\\~\\!]?)([1248aihsb])@([0-9a-z_]+)=([0-9a-z&=:\\-\\.\\/\\\\]+|\\*)$",
                Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(tmp);
        if(matcher.find()){
            hostOrder = matcher.group(1);
            type = matcher.group(2).toLowerCase();
            name = matcher.group(3);
            value = matcher.group(4);
        }else
            throw new IllegalArgumentException("字段错误 " + str);

        boolean ifNetOrder = true;
        if(config.order == Order.HOST)
            ifNetOrder = false;
        else
            ifNetOrder = true;
        if(!hostOrder.equals(""))
            ifNetOrder = !ifNetOrder;

        boolean ifValueCare = true;
        if(value.equals("*")){
            ifValueCare = false;
            value = "";
        }else if(value.indexOf("super.") != -1){
            value = RequestMessage.getReqValueByName(value.substring(value.indexOf(".") + 1));
            ifValueCare = false;
        }
        else
            ifValueCare = true;
        char fieldType = type.charAt(0);
        Field data = null;
        switch (fieldType){
            case '1':
                data = new OneByte(name, value, ifNetOrder, ifValueCare);
                break;
            case '2':
                data = new TwoBytes(name, value, ifNetOrder, ifValueCare);
                break;
            case '4':
                data = new FourBytes(name, value, ifNetOrder, ifValueCare);
                break;
            case '8':
                data = new EightBytes(name, value, ifNetOrder, ifValueCare);
                break;
            case 'i':
                data = new IP(name, value, ifNetOrder, ifValueCare);
                break;
            case 'h':
                data = new Hex(name, value, ifNetOrder, ifValueCare);
                break;
            case 's':
                data = new StringBytes(name, value, ifNetOrder, ifValueCare);
                break;
            case 'a':
                //data = new Array(name, value, ifNetOrder, ifValueCare);Array作为新数组
                data = new FourBytes(name, value, ifNetOrder, ifValueCare);//兼容以前的类型
                break;
            case 'b':
                data = new HttpBody(name, value, ifNetOrder, ifValueCare);
                break;
            default:
                throw new IllegalStateException("数据类型错误，不存在" + type + "@类型数据");
        }
        return data;
    }

    //不存在返回null
    public static String getSection(String p2pStr, String section){
        p2pStr = p2pStr.replaceAll("#.*[\r|\r\n]", "\r\n");//去除注释
        p2pStr = p2pStr.replaceAll("#.*$", "\r\n");//去除注释
        Pattern pattern = Pattern.compile("\\[([^\\[|\\]]+)\\]([^\\[|\\]]*)",
                Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE );
        Matcher matcher = pattern.matcher(p2pStr);
        while(matcher.find()){
            if(matcher.group(1).trim().toLowerCase().equals(section)){
                for(String tmp : HTTPHEADER){//HTTP头部不忽略中间空行
                    if(tmp.equals(section))
                        return matcher.group(2).trim();
                }
                return matcher.group(2).trim().replaceAll("\r\n\r\n","\r\n").replaceAll("\n\n", "\n");
            }
        }
        return null;
    }

    public static String getHttpHeaderSec(String p2pStr){
        String ret = null;
        for(int i = 0; i < HTTPHEADER.length; i++){
            if((ret = getSection(p2pStr, HTTPHEADER[i])) != null){
                String strs[] = ret.split("\n");
                ret = "";
                for(String tmp : strs){
                    ret += tmp.trim() + "\r\n";
                }
                return ret.trim();
            }

        }
        return ret;
    }

    public static String getConfigSec(String p2pStr){
        String ret = null;
        for(int i = 0; i < CONFIG.length; i++){
            if((ret = getSection(p2pStr, CONFIG[i])) != null)
                return ret;
        }
        return ret;
    }

    public static String getLHeaderSec(String p2pStr){
        String ret = null;
        for(int i = 0; i < LONGHEADER.length; i++){
            if((ret = getSection(p2pStr, LONGHEADER[i])) != null)
                return ret;
        }
        return ret;
    }

    public static String getHeaderSec(String p2pStr){
        String ret = null;
        for(int i = 0; i < HEADER.length; i++){
            if((ret = getSection(p2pStr, HEADER[i])) != null)
                return ret;
        }
        return ret;
    }

    public static String getBodySec(String p2pStr){
        String ret = null;
        for(int i = 0; i < BODY.length; i++){
            if((ret = getSection(p2pStr, BODY[i])) != null)
                return ret;
        }
        return ret;
    }

    static final String[] HTTPHEADER = {"http", "httpheader"};
    static final String[] CONFIG = {"config", "conf", "c"};
    static final String[] LONGHEADER = {"lhead", "lheader", "l", "longheader"};
    static final String[] HEADER = {"head", "header", "h"};
    static final String[] BODY = {"body", "b"};

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

    public static void main(String[] args) throws IOException {
        /*
        System.out.println(parseByte("-128"));i
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
        System.out.println(parseLong("18446744073709551615"));*/
        String input = "s@logdata = clusterid=2001&";
        System.out.println(strToField(input, new MsgConfig()));
    }
}
