package com.zm.data;


import com.zm.utils.BU;

/**
 * Created by zhangmin on 2015/9/22.
 */
abstract public class Field {

    //编码
    abstract public void encode(BufferMgr bufferMgr);

    //解码
    abstract public void decode(BufferMgr bufferMgr);

    //返回实际值
    public String getValue(){
        return strValue;
    }

    @Override
    public String toString(){
        return getValue();
    }

    //判断两个对象值是否相等
    public boolean equals(Field other){
        //另一个对象为空或类型不同
        if(other == null || this.getClass() != other.getClass())
            return false;
        //对象相同，本对象不关心值，另一个对象不关心值，返回真
        if(this == other || ! this.valueCare || ! other.valueCare)
            return true;
        //判断转换后值是否相同
        return this.strValue.equals(other.strValue);
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginValue() {
        return originValue;
    }

    public void setOriginValue(String originValue) {
        this.originValue = originValue;
    }

    public boolean isNetByte() {
        return netByte;
    }

    public void setNetByte(boolean netByte) {
        this.netByte = netByte;
    }

    public boolean isValueCare() {
        return valueCare;
    }

    public void setValueCare(boolean valueCare) {
        this.valueCare = valueCare;
    }

    public Field(String name, String originValue) {
        this.name = name;
        this.originValue = originValue;
    }

    public Field(String name, String originValue, boolean netByte, boolean valueCare) {
        this.name = name;
        this.originValue = originValue;
        this.netByte = netByte;
        this.valueCare = valueCare;
    }

    public Field(String name){
        this.name = name;
    }

    public Field(String name, boolean netByte, boolean valueCare) {
        this.name = name;
        this.netByte = netByte;
        this.valueCare = valueCare;
    }

    //名称
    protected String name = "";
    //原始值，可能与转换值不同，如"255"，在单字节的转换值为"-1"
    protected String originValue = "";
    //转换后的字符串值，两个对象值比较也是转换后字符串值的比较
    protected String strValue = "";
    //字节序，默认网络序
    protected boolean netByte = true;
    //是否关心值的大小，默认关心
    protected boolean valueCare = true;
}

