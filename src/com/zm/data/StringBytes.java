package com.zm.data;

import com.zm.utils.BU;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class StringBytes extends Field {
    @Override
    public void encode(BufferMgr bufferMgr) {
        len = originValue.length();
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
        this.originValue = strValue;
    }

    public StringBytes(String name, String originValue) {
        super(name, originValue);
    }

    public StringBytes(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
    }

    public StringBytes(String name) {
        super(name);
    }

    public StringBytes(String name, boolean hostByte, boolean valueCare) {
        super(name, hostByte, valueCare);
    }

    private int len = 0;

    public static void main(String[] args){
        BufferMgr mgr = new BufferMgr();

        Field[] list = new Field[8];
        list[0] = new OneByte("one", "255", true, true);
        list[1] = new TwoBytes("two", "255", true, true);
        list[2] = new FourBytes("four", "255", true, true);
        list[3] = new IP("ip", "127.0.0.1", true, true);
        list[4] = new Array("array", "2", true, true);
        list[5] = new EightBytes("eight", "255", true, true);
        list[6] = new Hex("hex", "abcde", true, true);
        list[7] = new StringBytes("string", "abcde", true, true);
        for(int i = 0; i < list.length; i++)
            list[i].encode(mgr);

        Field[] list2 = new Field[8];
        list2[0] = new OneByte("one", false, false);
        list2[1] = new TwoBytes("two", false, false);
        list2[2] = new FourBytes("four", false, false);
        list2[3] = new IP("ip", false, false);
        list2[4] = new Array("array", false, false);
        list2[5] = new EightBytes("eight", false, false);
        list2[6] = new Hex("hex", false, false);
        list2[7] = new StringBytes("string", false, false);
        for(int i = 0; i < list.length; i++)
            list2[i].encode(mgr);

        System.out.println(BU.bytes2HexGoodLook(mgr.getBuffer()));
        for(int i = 0; i < list.length; i++){
            System.out.println(list[i] + " : " + list2[i]);
            System.out.println(list[i].equals(list2[i]));
        }
    }
}
