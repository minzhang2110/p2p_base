package com.zm.Field;


import com.zm.message.BufferMgr;

/**
 * Created by zhangmin on 2015/9/22.
 *
 * originValue有初始值
 * strValue和value必须保持一致，和len一起随时更新
 * 字段比较是strValue的比较
 */
abstract public class Field{

    //编码
    abstract public void encode(BufferMgr bufferMgr);

    //解码
    abstract public void decode(BufferMgr bufferMgr);

    //返回字段所占的字节长度
    abstract public int getLen();

    //初始化originValue
    abstract protected void initOriginValue();

    @Override
    public String toString(){
        return getStrValue();
    }

    public String getName(){
        return name;
    }

    public void setOriginValue(String originValue) {
        if(originValue == null || (originValue != null && originValue.equals("")))
            initOriginValue();
        else
            this.originValue = originValue;
    }

    public String getStrValue() {
        return strValue;
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

    //判断两个对象值是否相等
    public CompareResult compare(Field other){
        if(other == null)
            return new CompareResult(false, "对象为空");

        if(this.getClass() != other.getClass())
            return new CompareResult(false, "类型不同，预期" + this.getName() + "是" + this.getClass() +
                    "，而实际" + other.getName()+ "是" + other.getClass());

        if(this == other || ! this.valueCare || ! other.valueCare)
            return new CompareResult(true, "");

        if(this.strValue.toLowerCase().equalsIgnoreCase(other.strValue))
            return new CompareResult(true, "");
        else
            return new CompareResult(false, "值不同，预期" + this.getName() + "是" + this +
                    "，而实际" + other.getName()+ "是" + other);
    }

    public Field(String name, String originValue) {
        if(name == null)
            this.name = "null";
        else
            this.name = name;
        if(originValue == null || (originValue != null && originValue.equals("")))
            initOriginValue();
        else
            this.originValue = originValue;
    }

    public Field(String name, String originValue, boolean netByte, boolean valueCare) {
        this(name, originValue);
        this.netByte = netByte;
        this.valueCare = valueCare;
    }

    public String getOriginValue() {
        return originValue;
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

