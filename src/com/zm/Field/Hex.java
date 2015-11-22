package com.zm.Field;

import com.zm.message.BufferMgr;
import com.zm.utils.BU;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class Hex extends Field {
    @Override
    public void encode(BufferMgr bufferMgr) {
        if(originValue.length() % 2 == 0)
            strValue = originValue;
        else
            strValue = "0" + originValue;
        try {
            if(netByte){
                bufferMgr.putBuffer(BU.int2Bytes(len));
                bufferMgr.putBuffer(BU.hex2Bytes(originValue));
            }else {
                bufferMgr.putBuffer(BU.int2Bytes_h(len));
                bufferMgr.putBuffer(BU.hex2Bytes_h(originValue));
            }
        }catch (Exception e){
            throw new IllegalStateException("[" + name + "] 编码失败 : " + e.getMessage());
        }

    }

    @Override
    public void decode(BufferMgr bufferMgr) {
        byte[] tmp = null;
        if((tmp = bufferMgr.getBuffer(4)) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        if(netByte)
            len = BU.bytes2Int(tmp);
        else
            len = BU.bytes2Int_h(tmp);
        if((tmp = bufferMgr.getBuffer(len)) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        if(netByte)
            strValue = BU.bytes2Hex(tmp);
        else
            strValue = BU.bytes2Hex_h(tmp);
    }

    @Override
    protected void initOriginValue() {
        this.originValue = "";
    }

    @Override
    public int getLen() {
        return len + 4;
    }

    @Override
    public void setOriginValue(String originValue){
        super.setOriginValue(originValue);
        len = (this.originValue.length() % 2 == 0)? this.originValue.length() / 2 : this.originValue.length() / 2 + 1;
    }

    public Hex(String name, String originValue) {
        super(name, originValue);
        len = (this.originValue.length() % 2 == 0)? this.originValue.length() / 2 : this.originValue.length() / 2 + 1;
    }

    public Hex(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
         len = (this.originValue.length() % 2 == 0)? this.originValue.length() / 2 : this.originValue.length() / 2 + 1;
    }

    private int len = 0;
}
