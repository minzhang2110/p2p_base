package com.zm.message;

import com.zm.Field.CompareResult;
import com.zm.Field.Field;
import com.zm.Field.OneByte;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/22.
 */
public class MsgBody {
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
    public int getLen(){
        int len = 0;
        for(int i = 0; i < list.size(); i++)
            len += list.get(i).getLen();
        return len;
    }
    public CompareResult compare(MsgBody other){
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

    public MsgBody(ArrayList<Field> list, boolean valueCare){
        if(list == null || list.size() == 0){
            this.list = new ArrayList<Field>();
            this.list.add(new OneByte("ComandId", ""));
        }else{
            if(list.get(0).getLen() != 1){
                throw new IllegalArgumentException(list.get(0).getName() +
                        "预期是ComandId,1个字节，而实际是" + list.get(0).getLen());
            }else
                this.list = list;
        }
        this.valueCare = valueCare;

    }

    public ArrayList<Field> list = null;
    private boolean valueCare = true;
}
