package com.zm.encryption;

import com.zm.utils.BU;

import java.util.Date;

/**
 * Created by zhangmin on 2015/11/14.
 */
public class MHXY_UDP {
    static {
        System.loadLibrary("p2p_encrypt");
    }

    public static native byte[] encrypt(int type, byte[] in_buffer, int in_len);

    public static native byte[] decrypt(byte[] in_buffer, int in_len);

    public static void main(String[] args){

        byte[] one = "abc".getBytes();
        byte[] two = encrypt(1, one, one.length);
        //byte[] two = BU.hex2Bytes("256723C6697351FFDBF9313494");
        byte[] there = decrypt(two, two.length);

        System.out.println(BU.bytes2HexGoodLook(one));
        System.out.println(BU.bytes2HexGoodLook(two));
        System.out.println(BU.bytes2HexGoodLook(there));
    }
}