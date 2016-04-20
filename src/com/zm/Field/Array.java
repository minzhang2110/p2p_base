package com.zm.Field;

import com.zm.message.BufferMgr;
import com.zm.utils.BU;
import com.zm.utils.U;

import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Created by zhangmin on 2015/11/13.
 * 根据valueCare决定是否比较数组大小，
 *
 * 无论Array的valueCare是什么，都需要比较数组
 *
 * 为了打印更加直观，加入了scope来判断第几层数组
 */
public class Array extends Field {

    @Override
    public void encode(BufferMgr bufferMgr) {
        //实际编码的是数组的长度
        value = groupList.size();
        if(netByte)
            bufferMgr.putBuffer(BU.int2Bytes(value));
        else
            bufferMgr.putBuffer(BU.int2Bytes_h(value));
        for(int i = 0; i < groupList.size(); i++){
            Field[] tmp = groupList.get(i);
            for(int j = 0; j < tmp.length; j++){
                tmp[j].encode(bufferMgr);
            }
        }

        //在compare时比较originValue
        try{
            value = U.parseInt(originValue);
        }catch (Exception e){
            throw new IllegalStateException("[" + name + "] 编码失败 : " + e.getMessage());
        }
        strValue = "" + value;
    }

    @Override
    public void decode(BufferMgr bufferMgr) {
        byte[] tmp = null;
        if((tmp = bufferMgr.getBuffer(4)) == null)
            throw new IllegalStateException("[" + name + "] 解码失败");
        if(netByte)
            value = BU.bytes2Int(tmp);
        else
            value = BU.bytes2Int_h(tmp);
        this.strValue = "" + value;
        balanceGroupList(value);
        for(int i = 0; i < groupList.size(); i++){
            Field[] tmpGroup = groupList.get(i);
            for(int j = 0; j < tmpGroup.length; j++){
                tmpGroup[j].decode(bufferMgr);
            }
        }
    }

    @Override
    public int getLen() {
        int len  = 4;
        for(int i = 0; i < groupList.size(); i++){
            Field[] tmp = groupList.get(i);
            for(int j = 0; j < tmp.length; j++){
                len += tmp[j].getLen();
            }
        }
        return len;
    }

    @Override
    protected void initOriginValue() {
        this.originValue = "0";
    }

    public CompareResult compare(Field other){/////////////////
        if(other == null)
            return new CompareResult(false, "对象为空");

        if(this.getClass() != other.getClass())
            return new CompareResult(false, "类型不同，预期" + this.getName() + "是" + this.getClass() +
                    "，而实际" + other.getName()+ "是" + other.getClass());

        if(this == other)
            return new CompareResult(true, "");

        if(this.valueCare && other.valueCare){
            if(!this.strValue.equals(other.strValue))//检查指定数组个数与实际数组个数是否相同
                return new CompareResult(false, "指定数组个数与实际不同，指定" + this.getName() + "是" + this.strValue +
                        "个，而实际" + other.getName()+ "是" + other.strValue + "个");
            else if(this.strValue.equals("0")){//两个数组个数相同且都为0，则不再往下比较
                return new CompareResult(true, "");
            }
        }



        ArrayList<Field[]> otherList = ((Array) other).groupList;
        if(this.groupList.size() > otherList.size()){//检查编写数组个数应比实际小
            return new CompareResult(false, "编写数组个数大于实际，编写" + this.getName() + "是" + this.groupList.size() +
                    "个，而实际" + other.getName()+ "是" + otherList.size() + "个");
        }

        CompareResult result = null;
        for(int i = 0; i < this.groupList.size(); i++){
            Field[] myGroup = groupList.get(i);
            for(int j = 0; j < otherList.size(); j++){
                Field[] otherGroup = otherList.get(j);
                result = compareGroup(myGroup, otherGroup);
                if(result.equal)
                    break;
            }
            if(!result.equal){
                return new CompareResult(false, this.getName() + "预期的第" + (i+1) + "个数组,在实际"+ other.getName() + "中没有找到");
            }
        }
        return new CompareResult(true, "");
    }

    private CompareResult compareGroup(Field[] my, Field[] other){
        CompareResult result = null;
        for(int i = 0; i < my.length; i++){
            result = my[i].compare(other[i]);
            if(!result.equal)
                return result;
        }
        return result;
    }

    public String toString(int scope){
        StringBuilder ret = new StringBuilder();
        ret.append(groupList.size() + "\r\n");//打印的是实际的数组个数，与预期的可能不一致
        int nextScope = scope + 1;
        for(int i = 0; i < groupList.size(); i++){
            Field[] tmp = groupList.get(i);
            ret.append(getCharString(' ', scope*3) + getCharString('-', 30-scope*3) + "\r\n");
            for(int j = 0; j < tmp.length; j++){
                if(tmp[j] instanceof Array)
                    ret.append(getCharString(' ', scope*3) + (tmp[j].getName() + "=" + ((Array)tmp[j]).toString(nextScope) + "\r\n"));
                else
                    ret.append(getCharString(' ', scope*3) + tmp[j].getName() + "=" + tmp[j] + "\r\n");
            }
        }
        ret.append(getCharString(' ', scope*3)  + getCharString('-', 30-scope*3));
        return ret.toString();
    }

    private String getCharString(char c, int len){
        StringBuilder ret = new StringBuilder();
        for(int i=0; i<len; i++){
            ret.append(c);
        }
        return ret.toString();
    }

    private void balanceGroupList(int expectNum){
        int factNum = groupList.size();
        if(factNum == expectNum)
            return;
        if(factNum > expectNum){
            for(int i = factNum; i > expectNum; i--)
                groupList.remove(i - 1);
        }
        if(expectNum > factNum){
            for(int i = factNum; i < expectNum; i++)
                groupList.add(copyGroupByFirst(this, i));
        }
    }

    private Field[] copyGroupByFirst(Array src, int num){
        Field[] firstGroup = src.groupList.get(0);
        Field[] copyGroup = new Field[firstGroup.length];
        try {
            for(int i = 0; i < firstGroup.length; i++){
                Class fieldType = firstGroup[i].getClass();
                Constructor constructor = fieldType.getConstructor(new Class[]{String.class,
                        String.class, Boolean.TYPE, Boolean.TYPE});

                copyGroup[i] = (Field) constructor.newInstance(new Object[]{firstGroup[i].getName() + num,
                        "", firstGroup[i].netByte, false});

                if(copyGroup[i] instanceof Array){
                    ((Array)copyGroup[i]).groupList.add(copyGroupByFirst((Array)firstGroup[i], num));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return copyGroup;
    }

    public Array(String name, String originValue) {
        super(name, originValue);
    }

    public Array(String name, String originValue, boolean hostByte, boolean valueCare) {
        super(name, originValue, hostByte, valueCare);
    }

    public static void main(String[] args){
        Array array = new Array("studentList", "2");
        Field[] group1 = new Field[2];
        group1[0] = new FourBytes("haha", "1");
        group1[1] =new TwoBytes("heihei", "2");
        Field[] group2 = new Field[2];
        group2[0] = new FourBytes("haha1", "9");
        group2[1] =new TwoBytes("heihei1", "4");
        array.groupList.add(group1);
        array.groupList.add(group2);

        Array array2 = new Array("studentList2", "2");
        Field[] group3 = new Field[2];
        group3[0] = new FourBytes("hahaha", "1");
        group3[1] = new TwoBytes("heiheihei", "2");
        //group3[1] = array;
        Field[] group4 = new Field[2];
        group4[0] = new FourBytes("hahaha1", "3");
        group4[1] =new TwoBytes("heiheihei1", "4");
        array2.groupList.add(group3);
        array2.groupList.add(group4);

        BufferMgr bufferMgr = new BufferMgr();
        array.encode(bufferMgr);
        array2.encode(bufferMgr);
        //array2.encode(bufferMgr);

        System.out.println(BU.bytes2HexGoodLook(bufferMgr.getBuffer()));
        System.out.println(array.compare(array2));

        System.out.println(array.getName() + "=" + array.toString(1));
        System.out.println(array2.getName() + "=" + array2.toString(1));
        //System.out.println(array2);

    }

    private int value = 0;
    public ArrayList<Field[]> groupList = new ArrayList<Field[]>();
}
