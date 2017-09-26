package com.zm.encryption;

import com.zm.utils.BU;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("需要文件名作为参数");
            System.exit(1);
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(args[0]));
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在" + args[0]);
            System.exit(1);
        }
        char[] buffer = new char[4096];
        StringBuffer sb = new StringBuffer();
        int len;
        while((len = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, len);
        }

        reader.close();
        String hex = sb.toString();

        if(hex.length() < 24){
            throw new IllegalStateException("AES解密失败, 原因 : 数据小于12字节");
        }
        hex = hex.trim().replaceAll("\\s", "");

        byte[] two = BU.hex2Bytes(hex);
        byte[] three = decryptEx(two, two.length);
        if (three != null) {
            System.out.println(BU.bytes2HexGoodLook(three));
        } else {
            System.out.println("解密失败");
        }
    }
}