package com.zm.Field;

import com.zm.message.BufferMgr;

/**
 * Created by Administrator on 2016/1/17.
 */
public class HttpBody extends Field {
    public HttpBody(String name, String originValue) {
        super(name, originValue);
    }

    public HttpBody(String name, String originValue, boolean netByte, boolean valueCare) {
        super(name, originValue, netByte, valueCare);
    }

    @Override
    public void encode(BufferMgr bufferMgr) {
        bufferMgr.putBuffer(originValue.getBytes());
        strValue = originValue;
    }

    @Override
    public void decode(BufferMgr bufferMgr) {
        byte[] tmp = null;
        if((tmp = bufferMgr.getAllLeft()) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        strValue = new String(tmp);
    }

    @Override
    public int getLen() {
        return strValue.length();
    }

    @Override
    protected void initOriginValue() {
        this.originValue = "";
    }
}
