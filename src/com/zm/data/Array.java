package com.zm.data;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class Array extends FourBytes {

    public Array(String name, String originValue) {
        super(name, originValue);
    }

    public Array(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
    }

    public Array(String name) {
        super(name);
    }

    public Array(String name, boolean hostByte, boolean valueCare) {
        super(name, hostByte, valueCare);
    }
}
