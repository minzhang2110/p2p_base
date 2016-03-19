package com.zm.Field;

import com.zm.message.BufferMgr;
import com.zm.utils.BU;
import com.zm.utils.U;

import java.io.BufferedWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Created by zhangmin on 2015/11/13.
 * 根据valueCare决定是否比较数组大小，
 *
 * 无论Array的valueCare是什么，都需要比较数组
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

        if(this.valueCare && other.valueCare)
            if(!this.strValue.equals(other.strValue))//检查指定数组个数与实际数组个数是否相同
                return new CompareResult(false, "指定数组个数与实际不同，指定" + this.getName() + "是" + this.strValue +
                        "个，而实际" + other.getName()+ "是" + other.strValue + "个");

        ArrayList<Field[]> otherList = ((Array) other).groupList;
        if(this.groupList.size() > otherList.size()){//检查编写数组个数应比实际小
            return new CompareResult(false, "编写数组个数大于实际，编写" + this.getName() + "是" + this.groupList.size() +
                    "个，而实际" + other.getName()+ "是" + otherList.size() + "个");
        }

        CompareResult result = null;
        for(int i = 0; i < this.groupList.size(); i++){
            Field[] myGroup = groupList.get(i);
            Field[] otherGroup = otherList.get(i);
            //由于预期和实际的包都来自同一个消息串，所以这种情况不会发生
            /*if(myGroup.length != otherGroup.length)
                return new CompareResult(false, "单个数组中元素个数不同，预期" + this.getName() + "是" + myGroup.length +
                        "个，而实际" + other.getName()+ "是" + otherGroup.length + "个");*/
            for(int j = 0; j < myGroup.length; j++){
                result = myGroup[j].compare(otherGroup[j]);
                if(!result.equal)
                    return result;
            }
        }
        return new CompareResult(true, "");
    }

    public String toString(){
        StringBuilder ret = new StringBuilder();
        ret.append(groupList.size() + "\r\n");
        for(int i = 0; i < groupList.size(); i++){
            Field[] tmp = groupList.get(i);
            ret.append("-----------------\r\n");
            for(int j = 0; j < tmp.length; j++){
                ret.append(tmp[j].getName() + "=" + tmp[j] + "\r\n");
            }

        }
        ret.append("--------------------");
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
        Array array = new Array("studentList", "2");
        Field[] group1 = new Field[2];
        group1[0] = new FourBytes("haha", "1");
        group1[1] =new TwoBytes("heihei", "2");
        Field[] group2 = new Field[2];
        group2[0] = new FourBytes("haha1", "3");
        group2[1] =new TwoBytes("heihei1", "4");
        array.groupList.add(group1);
        array.groupList.add(group2);

        Array array2 = new Array("studentList2", "2");
        Field[] group3 = new Field[2];
        group3[0] = new FourBytes("hahaha", "1");
        group3[1] =new TwoBytes("heiheihei", "2");
        Field[] group4 = new Field[2];
        group4[0] = new FourBytes("hahaha1", "3");
        group4[1] =new TwoBytes("heiheihei1", "4");
        array2.groupList.add(group3);
        array2.groupList.add(group4);

        BufferMgr bufferMgr = new BufferMgr();
        array.encode(bufferMgr);
        array2.decode(bufferMgr);
        //array2.encode(bufferMgr);

        System.out.println(BU.bytes2HexGoodLook(bufferMgr.getBuffer()));
        System.out.println(array.compare(array2));

        System.out.print(array.getName() + "=" + array);
        //System.out.println(array2);

    }

    private int value = 0;
    public ArrayList<Field[]> groupList = new ArrayList<Field[]>();
}
