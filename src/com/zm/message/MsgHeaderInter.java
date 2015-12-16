package com.zm.message;

import com.zm.Field.Field;
import com.zm.Field.FourBytes;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/23.
 */
public class MsgHeaderInter extends MsgHeader {
    @Override
    public boolean addBodyLen(int len, boolean valueCare) {
        if(list.size() == fieldSize.length){
            Field msgLen = new FourBytes("Bodylen", "" + len, true, valueCare);//网络序
            list.add(msgLen);
            return true;
        }
        return false;
    }

    public MsgHeaderInter(ArrayList<Field> list, boolean valueCare){
        super(list, valueCare);
        if(list == null){
            this.list = new ArrayList<Field>();
            this.list.add(new FourBytes("ProtocolVer", ""));
            this.list.add(new FourBytes("Sequence", ""));
        }else{
            if(list.size() != fieldSize.length)
                throw new IllegalArgumentException("交互协议头字段个数不正确，预期为" +
                        fieldSize.length + ",而实际是" + list.size());
            for(int i = 0; i < fieldSize.length; i++){
                if(list.get(i).getLen() != fieldSize[i])
                    throw new IllegalArgumentException("交互协议头字段长度不正确," +
                            list.get(i).getName() + "预期字节大小是" + fieldSize[i]);
            }
            this.list = list;
        }
        this.valueCare = valueCare;
    }

    private int[] fieldSize = {4, 4 };
}
