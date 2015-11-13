package com.zm.data;

import com.zm.utils.BU;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class Hex extends Field {
    @Override
    public void encode(BufferMgr bufferMgr) {
        len = (originValue.length() % 2 == 0)? originValue.length() / 2 : originValue.length() / 2 + 1;
        if(originValue.length() % 2 == 0){
            len =  originValue.length() / 2;
            strValue = originValue;
        } else{
            len = originValue.length() / 2 + 1;
            strValue = "0" + originValue;
        }
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
        this.originValue = strValue;
    }

    public Hex(String name, String originValue) {
        super(name, originValue);
    }

    public Hex(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
    }

    public Hex(String name) {
        super(name);
    }

    public Hex(String name, boolean hostByte, boolean valueCare) {
        super(name, hostByte, valueCare);
    }

    private int len = 0;

    public static void main(String[] args){
        BufferMgr mgr = new BufferMgr();

        Field[] list = new Field[5];
        list[0] = new Hex("result", "", false, true);
        list[0].encode(mgr);

        list[1] = new Hex("result", false, true);
        list[1].decode(mgr);
        System.out.println(list[0] + " : " + list[1]);
        System.out.println(BU.bytes2HexGoodLook(mgr.getBuffer()));
        System.out.println(list[0].equals(list[1]));
    }
}
