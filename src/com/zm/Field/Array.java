package com.zm.Field;

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
}
