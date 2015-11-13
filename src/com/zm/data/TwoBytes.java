package com.zm.data;

import com.zm.utils.BU;
import com.zm.utils.U;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class TwoBytes extends Field {
    @Override
    public void encode(BufferMgr bufferMgr) {
        try{
            value = U.parseShort(originValue);
        }catch (Exception e){
            throw new IllegalStateException("[" + name + "] 编码失败 : " + e.getMessage());
        }
        strValue = "" + value;
        if(netByte)
            bufferMgr.putBuffer(BU.short2Bytes(value));
        else
            bufferMgr.putBuffer(BU.short2Bytes_h(value));
    }

    @Override
    public void decode(BufferMgr bufferMgr) {
        byte[] tmp = null;
        if((tmp = bufferMgr.getBuffer(2)) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        if(netByte)
            value = BU.bytes2Short(tmp);
        else
            value = BU.bytes2Short_h(tmp);
        this.originValue = "" + value;
        this.strValue = "" + value;
    }

    public TwoBytes(String name, String originValue) {
        super(name, originValue);
    }

    public TwoBytes(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
    }

    public TwoBytes(String name) {
        super(name);
        this.originValue = "" + 0;
    }

    public TwoBytes(String name, boolean hostByte, boolean valueCare) {
        super(name, hostByte, valueCare);
        this.originValue = "" + 0;
    }

    private short value = 0;

    public static void main(String[] args){
        BufferMgr mgr = new BufferMgr();

        Field[] list = new Field[5];
        list[0] = new TwoBytes("result", "255");
        list[0].encode(mgr);

        list[1] = new TwoBytes("result", "255", false, true);
        list[1].encode(mgr);
        System.out.println(BU.bytes2HexGoodLook(mgr.getBuffer()));
    }
}
