package com.zm.Field;

import com.zm.message.BufferMgr;
import com.zm.utils.BU;

/**
 * Created by Administrator on 2016/9/7.
 */
public class HexLeft extends Field {
    public HexLeft(String name, String originValue) {
        super(name, originValue);
        len = (this.originValue.length() % 2 == 0)? this.originValue.length() / 2 : this.originValue.length() / 2 + 1;
    }

    public HexLeft(String name, String originValue, boolean netByte, boolean valueCare) {
        super(name, originValue, netByte, valueCare);
        len = (this.originValue.length() % 2 == 0)? this.originValue.length() / 2 : this.originValue.length() / 2 + 1;
    }

    @Override
    public void encode(BufferMgr bufferMgr) {
        if(originValue.length() % 2 == 0) {
            strValue = originValue;
        }
        else {
            strValue = "0" + originValue;
        }
        try {
            bufferMgr.putBuffer(BU.hex2Bytes(originValue));
        } catch (Exception e) {
            throw new IllegalStateException("[" + name + "] 编码失败 : " + e.getMessage());
        }
        bufferMgr.putBuffer(originValue.getBytes());
    }

    @Override
    public void decode(BufferMgr bufferMgr) {
        byte[] tmp = null;
        if((tmp = bufferMgr.getAllLeft()) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        strValue = BU.bytes2Hex(tmp);
        len = tmp.length;
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
