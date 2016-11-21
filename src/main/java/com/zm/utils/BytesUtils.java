package com.zm.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2015/9/20.
 * 默认：网络序(大端)，函数后缀‘_h’为主机序（小端）
 */
public class BytesUtils {
    public static byte[] subByte(byte[] data, int off, int length){
        if(off < 0 || (off + length) > data.length){
            throw new IllegalArgumentException("数组越界！");
        }
        byte[] ret = new byte[length];
        for(int i = off; i < (off + length); i++)
            ret[i - off] = data[i];
        return ret;
    }

    public static byte[] bytesMerger(byte[] arg1, byte[] arg2)
    {
        byte[] ret = new byte[arg1.length+arg2.length];
        int position = 0;
        for(int i=0; i<arg1.length; i++)
            ret[position++] = arg1[i];
        for(int i=0; i<arg2.length; i++)
            ret[position++] = arg2[i];
        return ret;
    }

    public static byte[] bytesMerger(byte[] arg1, byte arg2)
    {
        byte[] ret = new byte[arg1.length+1];
        int position = 0;
        for(int i=0; i<arg1.length; i++)
            ret[position++] = arg1[i];
        ret[position++] = arg2;
        return ret;
    }

    public static byte[] bytesMerger(byte[] arg1, byte[] arg2, int offset, int length)
    {
        if(offset < 0 || (offset + length) > arg2.length){
            throw new IllegalArgumentException("数组越界！");
        }
        byte[] ret = new byte[arg1.length+length];
        int position = 0;
        for(int i=0; i<arg1.length; i++)
            ret[position++] = arg1[i];
        for(int i=offset; i<(offset + length); i++)
            ret[position++] = arg2[i];
        return ret;
    }

    public static byte[] bytesReverse(byte[] data){
        byte[] ret = new byte[data.length];
        for(int i = 0; i < data.length; i++){
            ret[data.length - i - 1] = data[i];
        }
        return ret;
    }

    //查找到相应字节的位置,返回最后一次出现的位置
    //找不到返回-1
    public static int findLast(byte[] data, byte[] search){
        String str1 = new String(data);
        String str2 = new String(search);
        return str1.lastIndexOf(str2);
    }

    public static int findFirst(byte[] data, byte[] search){
        String str1 = new String(data);
        String str2 = new String(search);
        return str1.indexOf(str2);
    }

    public static String bytes2Hex(byte[] data){
        StringBuffer buf = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            if (((int) data[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int) data[i] & 0xff, 16));
        }
        return buf.toString();
    }

    public static String bytes2Hex_h(byte[] data){
        StringBuffer buf = new StringBuffer(data.length * 2);
        for (int i = data.length -1; i >=0 ; i--) {
            if (((int) data[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int) data[i] & 0xff, 16));
        }
        return buf.toString();
    }

    public static String bytes2Hex(byte[] data, int off, int length) {
        byte[] tmp = BytesUtils.subByte(data, off, length);
        return BytesUtils.bytes2Hex(tmp);
    }

    public static String bytes2Hex_h(byte[] data, int off, int length) {
        byte[] tmp = BytesUtils.subByte(data, off, length);
        return BytesUtils.bytes2Hex_h(tmp);
    }

    public static String bytes2HexGoodLook(byte[] data) {
        StringBuffer buf = new StringBuffer(data.length * 2);
        int len = 0;
        for (int i = 0; i < data.length; i++) {
            if (((int) data[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int) data[i] & 0xff, 16));
            len++;
            buf.append(" ");
            if(len%8 ==0)
                buf.append("\t");
            if(len%16 ==0)
                buf.append("\r\n");
        }
        return buf.toString();
    }

    public static String bytes2HexGoodLook_h(byte[] data) {
        StringBuffer buf = new StringBuffer(data.length * 2);
        int len = 0;
        for (int i = data.length -1; i >= 0 ; i--) {
            if (((int) data[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int) data[i] & 0xff, 16));
            len++;
            buf.append(" ");
            if(len%8 ==0)
                buf.append("\t");
            if(len%16 ==0)
                buf.append("\r\n");
        }
        return buf.toString();
    }

    public static String bytes2HexGoodLook(byte[] data, int off, int length) {
        byte[] tmp = BytesUtils.subByte(data, off, length);
        return BytesUtils.bytes2HexGoodLook(tmp);
    }

    public static String bytes2HexGoodLook_h(byte[] data, int off, int length) {
        byte[] tmp = BytesUtils.subByte(data, off, length);
        return BytesUtils.bytes2HexGoodLook_h(tmp);
    }

    public static byte[] hex2Bytes(String hexString)
    {
        if(null == hexString)
            throw new IllegalArgumentException("空指针错误");
        hexString = hexString.toUpperCase();
        if(hexString.length()%2 != 0)
            hexString = "0" + hexString;
        int dataLength = hexString.length()/2;
        byte[] data = new byte[dataLength];

        char high = ' ', low = ' ';
        byte b = 0;
        for(int i=0; i<dataLength; i++)
        {
            high = hexString.charAt(i*2);
            low = hexString.charAt(i*2+1);
            if(charToByte(high) == -1 || charToByte(low) == -1)
                throw new IllegalArgumentException("字符串中包含非法字符");
            b =(byte) (charToByte(high) << 4);
            b += charToByte(low);
            data[i] = b;
        }
        return data;
    }
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] hex2Bytes_h(String hexString)
    {
        if(null == hexString)
            throw new IllegalArgumentException("空指针错误");
        hexString = hexString.toUpperCase();
        if(hexString.length()%2 != 0)
            hexString = "0" + hexString;
        int dataLength = hexString.length()/2;
        byte[] data = new byte[dataLength];

        char high = ' ', low = ' ';
        byte b = 0;
        for(int i = 0; i < dataLength; i++)
        {
            high = hexString.charAt((dataLength - i -1)*2);
            low = hexString.charAt((dataLength - i -1)*2+1);
            if(charToByte(high) == -1 || charToByte(low) == -1)
                throw new IllegalArgumentException("字符串中包含非法字符");
            b =(byte) (charToByte(high) << 4);
            b += charToByte(low);
            data[i] = b;
        }
        return data;
    }

    public static byte[] short2Bytes(short num)
    {
        byte[] data = new byte[2];
        data[0] = (byte) (num >> 8 & 0xff);
        data[1] = (byte) (num &0xff);
        return data;
    }

    public static byte[] short2Bytes_h(short num)
    {
        byte[] data = new byte[2];
        data[1] = (byte) (num >> 8 & 0xff);
        data[0] = (byte) (num &0xff);
        return data;
    }

    public static short bytes2Short(byte[] data)
    {
        if(data.length != 2 || data == null)
            throw new IllegalArgumentException("数组长度不为2或空指针");
        short num = 0;
        return (short) ((data[0] & 0xff) << 8 | (data[1] & 0xff));
    }

    public static short bytes2Short_h(byte[] data)
    {
        if(data.length != 2 || data == null)
            throw new IllegalArgumentException("数组长度不为2或空指针");
        short num = 0;
        return (short) ((data[0] & 0xff)| (data[1] & 0xff) << 8 );
    }

    public static byte[] int2Bytes(int num)
    {
        byte[] data = new byte[4];
        data[0] = (byte) (num >> 24 & 0xff);
        data[1] = (byte) (num >> 16 & 0xff);
        data[2] = (byte) (num >> 8 & 0xff);
        data[3] = (byte) (num & 0xff);
        return data;
    }

    public static byte[] int2Bytes_h(int num)
    {
        byte[] data = new byte[4];
        data[3] = (byte) (num >> 24 & 0xff);
        data[2] = (byte) (num >> 16 & 0xff);
        data[1] = (byte) (num >> 8 & 0xff);
        data[0] = (byte) (num & 0xff);
        return data;
    }

    public static int bytes2Int(byte[] data)
    {
        if(data.length != 4 || data == null)
            throw new IllegalArgumentException("数组长度不为2或空指针");
        int num = 0;
        return (data[0] & 0xff) << 24 |(data[1] & 0xff) << 16 |(data[2] & 0xff) << 8 | (data[3] & 0xff);
    }

    public static int bytes2Int_h(byte[] data)
    {
        if(data.length != 4 || data == null)
            throw new IllegalArgumentException("数组长度不为2或空指针");
        int num = 0;
        return (data[0] & 0xff)|(data[1] & 0xff) << 8 |(data[2] & 0xff) << 16 | (data[3] & 0xff) << 24 ;
    }

    public static byte[] long2Bytes(long l){
        byte[] byteArray = new byte[8];
        for (int i=7; i>=0; i--){
            byteArray[i] = new Long(l & 0xff).byteValue();
            l >>= 8;
        }
        return byteArray;
    }

    public static byte[] long2Bytes_h(long l){
        byte[] byteArray = new byte[8];
        for (int i=7; i>=0; i--){
            byteArray[7 - i] = new Long(l & 0xff).byteValue();
            l >>= 8;
        }
        return byteArray;
    }

    public static long bytes2Long(byte[] b)
    {
        if(b.length != 8 || b == null)
            throw new IllegalArgumentException("数组长度不为8或空指针");
        long iOutcome = 0;
        byte bLoop;
        for (int i = 0; i < 8; i++)
        {
            bLoop = b[i];
            iOutcome += ((long)(bLoop & 0x000000ff)) << (8 * (7-i));
        }
        return iOutcome;
    }

    public static long bytes2Long_h(byte[] b)
    {
        if(b.length != 8 || b == null)
            throw new IllegalArgumentException("数组长度不为8或空指针");
        long iOutcome = 0;
        byte bLoop;
        for (int i = 0; i < 8; i++)
        {
            bLoop = b[7 - i];
            iOutcome += ((long)(bLoop & 0x000000ff)) << (8 * (7-i));
        }
        return iOutcome;
    }

    public static byte[] ip2Bytes(String ipString){
        if(!ipString.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}"))
            throw new IllegalArgumentException("ip地址错误");
        InetAddress ip = null;
        try {
             ip = InetAddress.getByName(ipString);
        }catch (UnknownHostException e){
            throw new IllegalArgumentException("ip地址解析失败");
        }
        return ip.getAddress();
    }

    public static byte[] ip2Bytes_h(String ipString){
        if(!ipString.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}"))
            throw new IllegalArgumentException("ip地址错误");
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName(ipString);
        }catch (UnknownHostException e){
            throw new IllegalArgumentException("ip地址解析失败");
        }
        return BytesUtils.bytesReverse(ip.getAddress());
    }

    public static String bytes2Ip(byte[] data){
        if(data.length != 4 || data == null)
            throw new IllegalArgumentException("数组长度不为4或空指针");
        String ip = "";
        try {
            ip = InetAddress.getByAddress(data).getHostAddress();
        }catch (UnknownHostException e){
            throw new IllegalArgumentException("ip数组解析失败");
        }
        return ip;
    }

    public static String bytes2Ip_h(byte[] data){
        if(data.length != 4 || data == null)
            throw new IllegalArgumentException("数组长度不为4或空指针");
        String ip = "";
        try {
            ip = InetAddress.getByAddress(BytesUtils.bytesReverse(data)).getHostAddress();
        }catch (UnknownHostException e){
            throw new IllegalArgumentException("ip数组解析失败");
        }
        return ip;
    }

    public static void main(String[] args){
        System.out.println(new String(BytesUtils.subByte("abcd".getBytes(), 0, 2)));
        System.out.println(new String(BytesUtils.bytesMerger("abcd".getBytes(), "ef".getBytes())));

        System.out.println(BytesUtils.bytes2Hex("abcdefghijklmnopqrstuvwxyz".getBytes()));
        System.out.println(BytesUtils.bytes2Hex_h("abcd".getBytes()));

        System.out.println(BytesUtils.bytes2Hex("abcd".getBytes(), 0, 2));
        System.out.println(BytesUtils.bytes2Hex_h("abcd".getBytes(), 0, 2));

        System.out.println(BytesUtils.bytes2HexGoodLook("abcdefghijklmnopqrstuvwxyz".getBytes()));
        System.out.println(BytesUtils.bytes2HexGoodLook_h("abcdefghijklmnopqrstuvwxyz".getBytes()));

        System.out.println(BytesUtils.bytes2HexGoodLook("abcdefghijklmnopqrstuvwxyz".getBytes(), 5, 3));
        System.out.println(BytesUtils.bytes2HexGoodLook_h("abcdefghijklmnopqrstuvwxyz".getBytes(), 5, 3));

        System.out.println(new String(BytesUtils.hex2Bytes("6162636465666768696a6b6c6d6e6f707172737475767778797a")));
        System.out.println(new String(BytesUtils.hex2Bytes_h("6162636465666768696a6b6c6d6e6f707172737475767778797a")));

        System.out.println(BytesUtils.bytes2Hex(short2Bytes((short) 258)));
        System.out.println(BytesUtils.bytes2Hex(short2Bytes_h((short) 258)));

        System.out.println(BytesUtils.bytes2Short(short2Bytes((short) 258)));
        System.out.println(BytesUtils.bytes2Short_h(short2Bytes_h((short) 258)));

        System.out.println(BytesUtils.bytes2Hex(int2Bytes(10001)));
        System.out.println(BytesUtils.bytes2Hex(int2Bytes_h(10001)));

        System.out.println(BytesUtils.bytes2Int(int2Bytes(10001)));
        System.out.println(BytesUtils.bytes2Int_h(int2Bytes_h(10001)));

        System.out.println(BytesUtils.bytes2Hex(long2Bytes(123123123l)));
        System.out.println(BytesUtils.bytes2Hex(long2Bytes_h(123123123l)));

        System.out.println(BytesUtils.bytes2Long(long2Bytes(123123123l)));
        System.out.println(BytesUtils.bytes2Long_h(long2Bytes_h(123123123l)));

        System.out.println(BytesUtils.bytes2Hex(BytesUtils.ip2Bytes("127.0.0.1")));
        System.out.println(BytesUtils.bytes2Hex(BytesUtils.ip2Bytes_h("127.0.0.1")));

        System.out.println(BytesUtils.bytes2Ip(BytesUtils.ip2Bytes("127.0.0.1")));
        System.out.println(BytesUtils.bytes2Ip_h(BytesUtils.ip2Bytes_h("127.0.0.1")));

        //System.out.println(BytesUtils.find("abcde\r\n\r\n".getBytes(), "\r\n\r\n".getBytes()));
    }
}
