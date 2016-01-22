package com.zm.Field;

import com.zm.message.BufferMgr;

/**
 * Created by Administrator on 2016/1/17.
 */
public class HttpBody extends Field {
    public HttpBody(String name, String originValue) {
        super(name, originValue);
        len = originValue.length();
    }

    public HttpBody(String name, String originValue, boolean netByte, boolean valueCare) {
        super(name, originValue, netByte, valueCare);
        len = originValue.length();
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
        len = strValue.length();
    }

    @Override
    public int getLen() {
        return len;
    }

    @Override
    protected void initOriginValue() {
        this.originValue = "";
    }
    int len = 0;
}
