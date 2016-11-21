package com.zm.message;

import com.zm.Field.EightBytes;
import com.zm.Field.Field;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/2/27.
 */
public class RequestMessage {
    public static ArrayList<Field> fieldList = new ArrayList<Field>();
    public static void registerAsReqMsg(Message msg){
        if(msg.longHeader != null)
            fieldList.addAll(msg.longHeader.list);
        if(msg.header != null)
            fieldList.addAll(msg.header.list);
        if(msg.msgBody != null)
            fieldList.addAll(msg.msgBody.list);
    }
    public static void clearReqMsg(){
        fieldList = new ArrayList<Field>();
    }
    public static String getReqValueByName(String name){
        for(int i = 0; i < fieldList.size(); i++){
            if(fieldList.get(i).getName().equals(name))
                return fieldList.get(i).toString();
        }
        return "";
    }
}
