package com.zm.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.zm.Field.Array;
import com.zm.Field.Field;
import com.zm.message.BufferMgr;
import com.zm.message.MsgConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;

/**
 * Created by Administrator on 2016/3/20.
 */
public class ArrayJson {
    //可能会语法错误，抛出异常
    public static Array parseJsonToArray(String json, MsgConfig config){
        ArrayJson aj = null;
        try {
            aj = new Gson().fromJson(json, ArrayJson.class);
        }catch (Exception e){
            throw new IllegalArgumentException(e.getMessage() + ": json语法错误: " + json);
        }
        if(aj.array == null){
            throw new IllegalArgumentException("json串中没有数组: " + json);
        }
        Array ret = null;
        try {
             ret = arrayJsonToArray(aj, config);
        }catch (Exception e){
            throw new IllegalArgumentException("可能json语法错误: " + json);
        }

        return ret;
    }

    private static Array arrayJsonToArray(ArrayJson aj, MsgConfig config){
        Field tmp = U.strToField(aj.getValue(), config);
        Array ret = new Array(tmp.getName(), tmp.getOriginValue(), tmp.isNetByte(), tmp.isValueCare());
        ArrayList<Field[]> groupList = new ArrayList<Field[]>();
        for(int i = 0; i < aj.array.size(); i++){
            ArrayJson[] groupJson = aj.array.get(i);
            Field[] group = new Field[groupJson.length];
            for(int j = 0; j < groupJson.length; j++){
                ArrayJson fieldJson = groupJson[j];
                Field field = null;
                if(fieldJson.array != null){
                    field = arrayJsonToArray(fieldJson, config);
                }else{
                    field = U.strToField(fieldJson.getValue(), config);
                }
                group[j] = field;
            }
            groupList.add(group);
        }
        ret.groupList = groupList;
        return ret;
    }

    public List<ArrayJson[]> getArray() {
        return array;
    }

    public void setArray(List<ArrayJson[]> array) {
        this.array = array;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private List<ArrayJson[]> array;
    private String value;
    public static void main(String[] args){
    /*    ArrayJson[] group1 = new ArrayJson[2];
        group1[0] = new ArrayJson("4@id=123", null);
        group1[1] = new ArrayJson("s@name=zhang", null);

        ArrayJson[] group2 = new ArrayJson[2];
        group2[0] = new ArrayJson("4@id=789", null);
        group2[1] = new ArrayJson("s@name=jqk", null);

        List<ArrayJson[]> list = new ArrayList<ArrayJson[]>();
        list.add(group1);
        list.add(group2);*/

        //ArrayTest2 arrayTest = new ArrayTest2("a@students=5", list);
        //Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Gson gson = new Gson();
        /*String input = "{\"value\": \"a@students=5\",\"array\": [\n" +
                "\t[{\"value\": \"4@id=123\"},{\"value\": \"a@students=9\",\"array\": [\n" +
                "    [{\"value\": \"4@id=123\"},{\"value\": \"s@name=zhang\"}],\n" +
                "    [{\"value\": \"4@id=789\"},{\"value\": \"s@name=jqk\"}]\n" +
                "]}]\n" +
                "]}";*/
        String input = "{\"value\": \"a@students=5\",\"array\": [\n" +
                "    [{\"value\": \"4@id=123\"},{\"value\": \"4@id=123\"}],\n" +
                "\t[{\"value\": \"4@id=123\"},{\"value\": \"4@id=123\"}]\n" +
                "]}";
        ArrayJson arrayTest = gson.fromJson(input, ArrayJson.class);

        Array array1 = null;
        try {
            array1 = ArrayJson.parseJsonToArray(input, new MsgConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferMgr bufferMgr = new BufferMgr();
        array1.encode(bufferMgr);
        System.out.println(BU.bytes2HexGoodLook(bufferMgr.getBuffer()));
        System.out.println(array1.getName() + "=" + array1.toString(1));
        System.out.println(new Gson().toJson(arrayTest));
    }
}
