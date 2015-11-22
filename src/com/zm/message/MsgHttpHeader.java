package com.zm.message;

import com.zm.Field.CompareResult;
import com.zm.Field.Field;
import com.zm.Field.FourBytes;
import com.zm.Field.OneByte;
import com.zm.utils.BU;

/**
 * Created by Administrator on 2015/11/22.
 */
public class MsgHttpHeader {

    public void encode(BufferMgr mgr){
        mgr.putBuffer(strValue.getBytes());
    }

    public void decode(BufferMgr mgr){
        byte[] tmp = null;
        if((tmp = mgr.getHttpHeader()) == null)
            throw new IllegalStateException("[HTTP] 解码失败");
        strValue = new String(tmp);
        strValue += "\r\n\r\n";
        len = strValue.length();
    }

    public int getLen(){
        return len;
    }

    public CompareResult compare(MsgHttpHeader other){
        if(other == null)
            return new CompareResult(false, "对象为空");

        if(this == other || ! this.valueCare || ! other.valueCare)
            return new CompareResult(true, "");
        if(this.strValue.equals(other.strValue))
            return new CompareResult(true, "");
        else
            return new CompareResult(false, "值不同，预期Http头部是" + this + "，而实际是" + other);
    }

    public String getStrValue() {
        return strValue;
    }

    @Override
    public String toString(){
        return getStrValue();
    }


    public MsgHttpHeader(String strValue, boolean valueCare){
        if(strValue == null)
            this.strValue = "\r\n\r\n";
        else{
            strValue = strValue.trim();
            this.strValue = strValue + "\r\n\r\n";
        }
        this.len = this.strValue.length();
        this.valueCare = valueCare;
    }

    private String strValue = "";
    private int len = 0;
    private boolean valueCare = true;

}
