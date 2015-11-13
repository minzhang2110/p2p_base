package com.zm.data;

import com.zm.utils.BU;
import com.zm.utils.U;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class EightBytes extends Field {
    @Override
    public void encode(BufferMgr bufferMgr) {
        try{
            value = U.parseLong(originValue);
        }catch (Exception e){
            throw new IllegalStateException("[" + name + "] 编码失败 : " + e.getMessage());
        }
        strValue = "" + value;
        if(netByte)
            bufferMgr.putBuffer(BU.long2Bytes(value));
        else
            bufferMgr.putBuffer(BU.long2Bytes_h(value));
    }

    @Override
    public void decode(BufferMgr bufferMgr) {
        byte[] tmp = null;
        if((tmp = bufferMgr.getBuffer(8)) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        if(netByte)
            value = BU.bytes2Long(tmp);
        else
            value = BU.bytes2Long_h(tmp);
        this.originValue = "" + value;
        this.strValue = "" + value;
    }

    public EightBytes(String name, String originValue) {
        super(name, originValue);
    }

    public EightBytes(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
    }

    public EightBytes(String name) {
        super(name);
        this.originValue = "0";
    }

    public EightBytes(String name, boolean hostByte, boolean valueCare) {
        super(name, hostByte, valueCare);
        this.originValue = "0";
    }

    private long value = 0;
}
