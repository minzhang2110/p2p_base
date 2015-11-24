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

        String hex = "00 00 00 65 00 00  00 00 00 00 00 60 87 ee \n" +
                "  15 b5 51 00 2f 41 97 d3  49 e1 50 41 f3 6b 44 a8 \n" +
                "  91 24 28 6c cf f0 de 5c  5c 77 59 7e c3 16 02 7f \n" +
                "  72 87 02 4c d1 03 c2 71  f4 f7 80 f2 d5 b1 52 70 \n" +
                "  e5 87 06 7a ca 92 02 cc  a8 97 23 19 55 00 e3 b7 \n" +
                "  3a 7a b5 7c fe 47 93 d9  fe 82 d7 94 39 3e b8 4b \n" +
                "  a5 a1 9a 73 aa 4f da 13  ef da f9 60 96 a3";
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