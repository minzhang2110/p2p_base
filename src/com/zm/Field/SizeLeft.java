package com.zm.Field;

import com.zm.message.BufferMgr;

/**
 * Created by zhangmin on 2016/9/7.
 */
public class SizeLeft extends Field {
    public SizeLeft(String name, String originValue) {
        super(name, originValue);
    }

    public SizeLeft(String name, String originValue, boolean netByte, boolean valueCare) {
        super(name, originValue, netByte, valueCare);
    }

    @Override
    public void encode(BufferMgr bufferMgr) {
        //不需要编码
    }

    @Override
    public void decode(BufferMgr bufferMgr) {
        this.strValue ="" + bufferMgr.dataCntLeftToDecode();
        bufferMgr.getAllLeft();
    }

    @Override
    public int getLen() {
        return 0;
    }

    @Override
    protected void initOriginValue() {
        this.originValue = "0";
    }
}
