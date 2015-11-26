package com.zm.message;

import com.zm.Field.CompareResult;
import com.zm.Field.Field;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/22.
 */
abstract public class MsgHeader {
    public void encode(BufferMgr mgr){
        for(int i = 0; i < list.size(); i++){
            list.get(i).encode(mgr);
        }
    }
    public void decode(BufferMgr mgr){
        for(int i = 0; i < list.size(); i++){
            list.get(i).decode(mgr);
        }
    }
    public String toString(){
        String ret = "";
        for(int i = 0; i < list.size(); i++){
            ret += list.get(i).getName() + "=" + list.get(i) + "\r\n";
        }
        return  ret;
    }

    public int getLen(){
        int len = 0;
        for(int i = 0; i < list.size(); i++)
            len += list.get(i).getLen();
        return len;
    }
    abstract public boolean addBodyLen(int len);
    public CompareResult compare(MsgHeader other){
        if(other == null)
            return new CompareResult(false, "对象为空");

        if(this.getClass() != other.getClass())
            return new CompareResult(false, "类型不同，预期是" + this.getClass() + "，而实际是" + other.getClass());

        if(this == other || ! this.valueCare || ! other.valueCare)
            return new CompareResult(true, "");

        if(this.list.size() != other.list.size())
            return new CompareResult(false, "交互（或UDP）协议头字段个数不一致，预期是" + this.list.size() +
                    "而实际是" + other.list.size());

        CompareResult result = null;
        for(int i = 0; i < this.list.size(); i++){
            result = this.list.get(i).compare(other.list.get(i));
            if(!result.equal)
                return result;
        }
        return new CompareResult(true, "");
    }

    public MsgHeader(ArrayList<Field> list, boolean valueCare){}

    public ArrayList<Field> list = null;
    protected boolean valueCare = true;
}
