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
    }

    public int getLen(){
        return strValue.length();
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

    public boolean addBodyLen(int contentLength){
        if(strValue.indexOf("Content-Length") == -1){
            strValue = strValue.trim() + "\r\nContent-Length: " + contentLength + "\r\n\r\n";
        }
        return false;
    }


    public MsgHttpHeader(String strValue, boolean valueCare){
        if(strValue == null)
            this.strValue = "";
        else if(strValue.indexOf("\r\n\r\n") == -1)
            this.strValue = strValue.trim() + "\r\n\r\n";
        else{
            this.strValue = strValue;
        }
        this.valueCare = valueCare;
    }

    private String strValue = "";
    private boolean valueCare = true;

}
