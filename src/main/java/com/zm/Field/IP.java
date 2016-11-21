package com.zm.Field;

import com.zm.message.BufferMgr;
import com.zm.utils.BU;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class IP extends Field {
    @Override
    public void encode(BufferMgr bufferMgr) {
        try{
            if(netByte)
                bufferMgr.putBuffer(BU.ip2Bytes(originValue));
            else
                bufferMgr.putBuffer(BU.ip2Bytes_h(originValue));
        }catch (Exception e){
            throw new IllegalStateException("[" + name + "] 编码失败 : " + e.getMessage());
        }
        strValue = originValue;
    }

    @Override
    public void decode(BufferMgr bufferMgr) {
        byte[] tmp = null;
        if((tmp = bufferMgr.getBuffer(4)) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        if(netByte)
            strValue = BU.bytes2Ip(tmp);
        else
            strValue = BU.bytes2Ip_h(tmp);
    }

    @Override
    protected void initOriginValue() {
        this.originValue = "0.0.0.0";
    }

    @Override
    public int getLen() {
        return 4;
    }

    public IP(String name, String originValue) {
        super(name, originValue);
    }

    public IP(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
    }
}
