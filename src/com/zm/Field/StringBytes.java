package com.zm.Field;

import com.zm.message.BufferMgr;
import com.zm.utils.BU;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class StringBytes extends Field {
    @Override
    public void encode(BufferMgr bufferMgr) {
        try {
            if(netByte){
                bufferMgr.putBuffer(BU.int2Bytes(len));
                bufferMgr.putBuffer(originValue.getBytes());
            }else {
                bufferMgr.putBuffer(BU.int2Bytes_h(len));
                bufferMgr.putBuffer(BU.bytesReverse(originValue.getBytes()));
            }
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
            len = BU.bytes2Int(tmp);
        else
            len = BU.bytes2Int_h(tmp);
        if((tmp = bufferMgr.getBuffer(len)) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        if(netByte)
            strValue = new String(tmp);
        else
            strValue = new String(BU.bytesReverse(tmp));
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
        len = this.originValue.length();
    }

    public StringBytes(String name, String originValue) {
        super(name, originValue);
        len = this.originValue.length();
    }

    public StringBytes(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
        len = this.originValue.length();
    }

    private int len = 0;

    public static void main(String[] args){
        BufferMgr mgr = new BufferMgr();
/*
        Field[] list = new Field[8];
        list[0] = new OneByte("one", "255", true, true); list[0].setOriginValue("-2");
        list[1] = new TwoBytes("two", "255", true, true); list[1].setOriginValue("-2");
        list[2] = new FourBytes("four", "255", true, true); list[2].setOriginValue("-2");
        list[3] = new IP("ip", "127.0.0.1", true, true); list[3].setOriginValue("1.1.1.1");
        list[4] = new Array("array", "2", true, true); list[4].setOriginValue("4");
        list[5] = new EightBytes("eight", "255", true, true); list[5].setOriginValue("-2");
        list[6] = new Hex("hex", "abcde", true, true); list[6].setOriginValue("efeff");
        list[7] = new StringBytes("string", "abcde", true, true); list[7].setOriginValue("-2");

        for(int i = 0; i < list.length; i++){
            list[i].encode(mgr);
            //System.out.print(list[i].getLen() + "\t");
            //System.out.println(list[i]);
        }
        //System.out.println(BU.bytes2HexGoodLook(mgr.getBuffer()));

*/
        Field[] list2 = new Field[8];
        list2[0] = new OneByte("one", null, true, true);
        list2[1] = new TwoBytes("two", null, true, true);
        list2[2] = new FourBytes("four", null, true, true);
        list2[3] = new IP("ip", null, true, true);
        list2[4] = new Array("array", null, true, true);
        list2[5] = new EightBytes("eight", null, true, true);
        list2[6] = new Hex("hex", null, true, true);
        list2[7] = new StringBytes("string", null, true, true);
        for(int i = 0; i < list2.length; i++)
            list2[i].encode(mgr);

        Field[] list3 = new Field[8];
        list3[0] = new OneByte("one", "");
        list3[1] = new TwoBytes("two", "");
        list3[2] = new FourBytes("four", "");
        list3[3] = new IP("ip", "");
        list3[4] = new Array("array", "");
        list3[5] = new EightBytes("eight", "");
        list3[6] = new Hex("hex", "");
        list3[7] = new StringBytes("string", "");
        for(int i = 0; i < list3.length; i++){
            list3[i].decode(mgr);
        }
        list3[2].setOriginValue("1");
        list3[2].encode(mgr);
        for(int i = 0; i < list3.length; i++) {
            System.out.println(list2[i].equals(list3[i]));
        }


    }
}
