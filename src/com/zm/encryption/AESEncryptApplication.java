package com.zm.encryption;

import com.zm.utils.BU;

/**
 * Created by zhangmin on 2015/11/14.
 */
public class AESEncryptApplication {
    static {
        System.loadLibrary("p2p_encrypt");
    }

    //需大于等于12个字节
    public static native byte[] encryptEx(byte[] in_buffer, int in_len);

    //需大于等于12个字节
    public static native byte[] decryptEx(byte[] in_buffer, int in_len);

    public static void main(String[] args) {

        byte[] one = BU.hex2Bytes("000000c814ffffffff101112");

        byte[] two = encryptEx(one, one.length);

        byte[] three = decryptEx(two, two.length);

        System.out.println(BU.bytes2Hex(one));
        System.out.println(BU.bytes2Hex(two));
        System.out.println(BU.bytes2Hex(three));
    }
}