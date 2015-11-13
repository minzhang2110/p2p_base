package com.zm.data;

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
        this.originValue = "" + value;
        this.strValue = "" + value;
    }

    public OneByte(String name, String originValue){
        super(name, originValue);
    }

    public OneByte(String name, String originValue, boolean hostByte, boolean valueCare)
    {
        super(name, originValue, hostByte, valueCare);
    }

    public OneByte(String name) {
        super(name);
        this.originValue = "" + 0;
    }

    public OneByte(String name, boolean hostByte, boolean valueCare) {
        super(name, hostByte, valueCare);
        this.originValue = "" + 0;
    }

    private byte value = 0;

    public static void main(String[] args){
        BufferMgr mgr = new BufferMgr();
        Field one = new OneByte("result", "255");
        one.encode(mgr);

        Field one2 = new OneByte("result", true, false);
        one2.decode(mgr);

        System.out.println(one.getValue());
        System.out.println(one2.getValue());
        System.out.println(one.equals(one2));
        System.out.println(one2.equals(one));
        System.out.println(BU.bytes2HexGoodLook(mgr.getBuffer()));

        Field one3 = new OneByte("result");
        one3.decode(mgr);
        System.out.println(one3.getValue());
    }
}
