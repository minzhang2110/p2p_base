package com.zm.message;

import com.zm.utils.BU;

/**
 * Created by zhangmin on 2015/11/13.
 */
public class BufferMgr {
    public BufferMgr(){
        buffer = new byte[0];
        decodeIndex = 0;
    }

    public BufferMgr(byte[] buffer){
        this.buffer = buffer;
        decodeIndex = 0;
    }

    public boolean empty(){
        return buffer.length == 0 || buffer == null;
    }

    public int Length(){
        return buffer.length;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
        this.decodeIndex = 0;
    }

    public byte[] getBuffer(int size){
        if(decodeIndex < 0 || size < 0 || (Length() - decodeIndex) < size)
            return null;
        byte[] ret = null;
        ret = BU.subByte(buffer, decodeIndex, size);
        decodeIndex += size;
        return ret;
    }

    //首先解码Http
    public byte[] getHttpHeader(){
        int index = BU.find(buffer, "\r\n\r\n".getBytes());
        if(index == -1)
            return null;
        decodeIndex = index + 4;
        return BU.subByte(buffer, 0, index);
    }

    public void putBuffer(byte[] data){
        buffer = BU.bytesMerger(buffer, data);
    }

    private byte[] buffer;
    private int decodeIndex;//解码位置
}