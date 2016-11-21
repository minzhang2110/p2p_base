package com.zm.Field;

import com.zm.message.BufferMgr;
import com.zm.utils.BU;
import com.zm.utils.U;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class OneByte extends Field {
    @Override
    public void encode(BufferMgr bufferMgr) {
        try{
            this.value = U.parseByte(originValue);
        }catch (Exception e){
            throw new IllegalStateException("[" + name + "] 编码失败 : " + e.getMessage());
        }

        this.strValue = "" + value;
        bufferMgr.putBuffer(new byte[]{ value });
    }

    @Override
    public void decode(BufferMgr bufferMgr) {
        byte[] tmp = null;
        if((tmp = bufferMgr.getBuffer(1)) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        value = tmp[0];
        this.strValue = "" + value;
    }

    @Override
    protected void initOriginValue() {
        this.originValue = "0";
    }

    @Override
    public int getLen() {
        return 1;
    }

    public OneByte(String name, String originValue){
        super(name, originValue);
    }

    public OneByte(String name, String originValue, boolean hostByte, boolean valueCare)
    {
        super(name, originValue, hostByte, valueCare);
    }

    public byte getValue() {
        return value;
    }

    private byte value = 0;
}
