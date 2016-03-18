package com.zm.Field;

import com.zm.message.BufferMgr;
import com.zm.utils.BU;
import com.zm.utils.U;

import java.io.BufferedWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class Array extends Field {

    @Override
    public void encode(BufferMgr bufferMgr) {
        try{
            value = U.parseInt(originValue);
        }catch (Exception e){
            throw new IllegalStateException("[" + name + "] 编码失败 : " + e.getMessage());
        }
        strValue = "" + value;
        if(netByte)
            bufferMgr.putBuffer(BU.int2Bytes(value));
        else
            bufferMgr.putBuffer(BU.int2Bytes_h(value));
        //根据value更改数组
        balanceGroupList(value);
        for(int i = 0; i < groupList.size(); i++){
            Field[] tmp = groupList.get(i);
            for(int j = 0; j < tmp.length; j++){
                tmp[j].encode(bufferMgr);
            }
        }
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
        return 0;
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

        if(this == other || ! this.valueCare || ! other.valueCare)
            return new CompareResult(true, "");

        ArrayList<Field[]> otherList = ((Array) other).groupList;
        if(this.groupList.size() != otherList.size()){
            return new CompareResult(false, "数组个数不同，预期" + this.getName() + "是" + this.groupList.size() +
                    "个，而实际" + other.getName()+ "是" + otherList.size() + "个");
        }

        CompareResult result = null;
        for(int i = 0; i < this.groupList.size(); i++){
            Field[] myGroup = groupList.get(i);
            Field[] otherGroup = otherList.get(i);
            if(myGroup.length != otherGroup.length)
                return new CompareResult(false, "单个数组中元素个数不同，预期" + this.getName() + "是" + myGroup.length +
                        "个，而实际" + other.getName()+ "是" + otherGroup.length + "个");
            for(int j = 0; j < myGroup.length; j++){
                result = myGroup[j].compare(otherGroup[j]);
                if(!result.equal)
                    return result;
            }
        }
        return new CompareResult(true, "");
    }

    public String toString(){//////////////////////////////
        StringBuilder ret = new StringBuilder();
        for(int j=0; j<this.groupList.size();j++){
            Field[] test = this.groupList.get(j);
            for(int i=0; i<test.length; i++){
                ret.append(test[i].getName() + "\t"
                        + test[i] + "\t"
                        + test[i].netByte + "\t"
                        + test[i].valueCare + "\t"
                        + test[i].getClass() + "\r\n");
            }
            ret.append("==========================\r\n");
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
                groupList.add(copyGroupByFirst(i));
        }
    }

    private Field[] copyGroupByFirst(int num){
        Field[] firstGroup = groupList.get(0);
        Field[] copyGroup = new Field[firstGroup.length];
        try {
            for(int i = 0; i < firstGroup.length; i++){
                Class fieldType = firstGroup[i].getClass();
                Constructor constructor = fieldType.getConstructor(new Class[]{String.class,
                        String.class, Boolean.TYPE, Boolean.TYPE});

                copyGroup[i] = (Field) constructor.newInstance(new Object[]{firstGroup[i].getName() + num,
                        "", firstGroup[i].netByte, false});
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
        Array array = new Array("array", "2");
        Field[] group1 = new Field[2];
        group1[0] = new FourBytes("haha", "1");
        group1[1] =new TwoBytes("heihei", "2");
        Field[] group2 = new Field[2];
        group2[0] = new FourBytes("haha1", "3");
        group2[1] =new TwoBytes("heihei1", "4");
        array.groupList.add(group1);
        array.groupList.add(group2);

        Array array2 = new Array("array", "5");
        Field[] group3 = new Field[2];
        group3[0] = new FourBytes("hahaha", "0");
        group3[1] =new TwoBytes("heiheihei", "0");
        //Field[] group4 = new Field[2];
        //group4[0] = new FourBytes("hahaha1", "3");
        //group4[1] =new TwoBytes("heiheihei1", "4");
        array2.groupList.add(group3);
        //array2.groupList.add(group4);

        BufferMgr bufferMgr = new BufferMgr();
        array.encode(bufferMgr);
        array2.decode(bufferMgr);

        System.out.println(BU.bytes2HexGoodLook(bufferMgr.getBuffer()));
        System.out.println(array.compare(array2));

        System.out.println(array);
        System.out.println(array2);

    }

    private int value = 0;
    public ArrayList<Field[]> groupList = new ArrayList<Field[]>();
}
