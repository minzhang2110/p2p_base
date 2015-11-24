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

        String hex = "00  00 00 65 00 00 00 00 00 \n" +
                "00 00 10 be b2 cd 8e 1f  81 26 37 35 ea 09 ab 76 \n" +
                "ee c4 fb";
        hex = hex.trim().replaceAll("\\s", "");
        //byte[] one = BU.hex2Bytes(hex);

        //byte[] two = encryptEx(one, one.length);
        byte[] two = BU.hex2Bytes(hex);
        byte[] three = decryptEx(two, two.length);

        //System.out.println(BU.bytes2Hex(one));
        System.out.println(BU.bytes2HexGoodLook(two));
        System.out.println("=========================");
        System.out.println(BU.bytes2HexGoodLook(three));
    }
}