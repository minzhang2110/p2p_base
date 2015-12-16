package com.zm.message;

import com.zm.Field.*;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/22.
 */
public class MsgLongHeader {
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

    public boolean addBodyLen(int len){
        if(list.size() == fieldSize.length){
            Field msgLen = new FourBytes("MsgLen", "" + len, true, false);//网络序,为避免BODY中有*的情况，暂时不对长连接头的msglen字段比较
            list.add(msgLen);
            return true;
        }
        return false;
    }

    public CompareResult compare(MsgLongHeader other){
        if(other == null)
            return new CompareResult(false, "对象为空");

        if(this == other || ! this.valueCare || ! other.valueCare)
            return new CompareResult(true, "");

        if(this.list.size() != other.list.size())
            return new CompareResult(false, "长连接协议头字段个数不一致，预期是" + this.list.size() +
            "而实际是" + other.list.size());

        CompareResult result = null;
        for(int i = 0; i < this.list.size(); i++){
            result = this.list.get(i).compare(other.list.get(i));
            if(!result.equal)
                return result;
        }
        return new CompareResult(true, "");
    }

    public MsgLongHeader(ArrayList<Field> list, boolean valueCare){
        if(list == null){
            this.list = new ArrayList<Field>();
            this.list.add(new TwoBytes("MsgId", null));
            this.list.add(new IP("SrcClientIp", null));
            this.list.add(new TwoBytes("MsgId", null));
            this.list.add(new EightBytes("SrcTaskId", null));
            this.list.add(new FourBytes("SrcProcessId", null));
            this.list.add(new EightBytes("DestTaskId", null));
            this.list.add(new FourBytes("DestProcessId", null));
        }
        else {
            if(list.size() != fieldSize.length)
                throw new IllegalArgumentException("长连接协议头字段个数不正确，预期为" +
                        fieldSize.length + ",而实际是" + list.size());
            for(int i = 0; i < fieldSize.length; i++){
                if(list.get(i).getLen() != fieldSize[i])
                    throw new IllegalArgumentException("长连接协议头字段长度不正确," +
                            list.get(i).getName() + "预期字节大小是" + fieldSize[i]);
            }
            this.list = list;
        }

        this.valueCare = valueCare;
    }
    public ArrayList<Field> list = null;
    private boolean valueCare = true;
    private int[] fieldSize = {2, 4, 2, 8, 4, 8, 4};
}
