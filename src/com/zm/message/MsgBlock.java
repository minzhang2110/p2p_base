package com.zm.message;

import com.zm.Field.Array;
import com.zm.Field.CompareResult;
import com.zm.Field.Field;

import java.util.ArrayList;

/**
 * Created by zhangmin on 2016/1/14.
 */
public class MsgBlock {

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
            if(list.get(i) instanceof Array)
                ret += list.get(i).getName() + "=" + ((Array)list.get(i)).toString(1) + "\r\n";
            else
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

    public boolean setBodyLen(int len){

        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getName().toLowerCase().indexOf("len") != -1){
                list.get(i).setValueCare(true);//强制比较msglen和bodylen
                for(int j = i + 1; j < list.size(); j++)
                    len += list.get(j).getLen();
                list.get(i).setOriginValue("" + len);
                return true;
            }
        }
        return false;
    }

    public CompareResult compare(MsgBlock other){
        if(other == null)
            return new CompareResult(false, "对象为空");

        if(this == other || ! this.valueCare || ! other.valueCare)
            return new CompareResult(true, "");

        if(this.list.size() != other.list.size())
            return new CompareResult(false, "协议体字段个数不一致，预期是" + this.list.size() +
                    "而实际是" + other.list.size());

        CompareResult result = null;
        for(int i = 0; i < this.list.size(); i++){
            result = this.list.get(i).compare(other.list.get(i));
            if(!result.equal)
                return result;
        }
        return new CompareResult(true, "");
    }

    public MsgBlock(ArrayList<Field> list, boolean valueCare){
        if(list == null || list.size() == 0) {
            throw new IllegalArgumentException("字段列表为NULL或空");
        }else{
            this.list = list;
            this.valueCare = valueCare;
        }

    }
    public ArrayList<Field> list = null;
    private boolean valueCare = true;
}
