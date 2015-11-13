package com.zm.data;

import com.zm.utils.BU;
import com.zm.utils.U;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class FourBytes extends Field {
    @Override
    public void encode(BufferMgr bufferMgr) {
        try{
            value = U.parseInt(originValue);
        }catch (Exception e){
            throw new IllegalStateException("[" + name + "] 编码失败 : " + e.getMessage());
        }
        strValue = "" + value;
        if(netByte)
            bufferMgr.putBuffer(BU.int2Bytes(value));
        else
            bufferMgr.putBuffer(BU.int2Bytes_h(value));
    }

    @Override
    public void decode(BufferMgr bufferMgr) {
        byte[] tmp = null;
        if((tmp = bufferMgr.getBuffer(4)) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        if(netByte)
            value = BU.bytes2Int(tmp);
        else
            value = BU.bytes2Int_h(tmp);
        this.originValue = "" + value;
        this.strValue = "" + value;
    }

    public FourBytes(String name, String originValue) {
        super(name, originValue);
    }

    public FourBytes(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
    }

    public FourBytes(String name) {
        super(name);
        this.originValue = "" + 0;
    }

    public FourBytes(String name, boolean hostByte, boolean valueCare) {
        super(name, hostByte, valueCare);
        this.originValue = "" + 0;
    }

    private int value = 0;

    public static void main(String[] args){
        BufferMgr mgr = new BufferMgr();

        Field[] list = new Field[5];
        list[0] = new FourBytes("result", "255");
        list[0].encode(mgr);

        list[1] = new FourBytes("result", "255", false, true);
        list[1].encode(mgr);
        System.out.println(BU.bytes2HexGoodLook(mgr.getBuffer()));
    }
}
