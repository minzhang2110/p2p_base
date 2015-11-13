package com.zm.data;

import com.zm.utils.BU;
import com.zm.utils.U;

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
        this.originValue = strValue;
    }

    public IP(String name, String originValue) {
        super(name, originValue);
    }

    public IP(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
    }

    public IP(String name) {
        super(name);
        this.originValue = "0.0.0.0";
    }

    public IP(String name, boolean hostByte, boolean valueCare) {
        super(name, hostByte, valueCare);
        this.originValue = "0.0.0.0";
    }

    public static void main(String[] args){
        BufferMgr mgr = new BufferMgr();

        Field[] list = new Field[5];
        list[0] = new IP("result", "127.0.0.1");
        list[0].encode(mgr);

        list[1] = new IP("result", "127.0.0.1", false, true);
        list[1].encode(mgr);
        System.out.println(BU.bytes2HexGoodLook(mgr.getBuffer()));
        System.out.println(list[0].equals(list[1]));
    }
}
