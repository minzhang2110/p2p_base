package com.zm.message;

import com.sun.xml.internal.messaging.saaj.packaging.mime.Header;
import com.zm.Field.CompareResult;
import com.zm.Field.Field;
import com.zm.Field.FourBytes;
import com.zm.encryption.AESEncryptApplication;
import com.zm.encryption.Encrypt;
import com.zm.encryption.MHXY_UDP;
import com.zm.utils.BU;
import com.zm.utils.U;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/11/22.
 * 已知问题：
 * 一、在字段中有*时，预期的Content-Length，msglen，bodylen与实际可能不同，现在的处理为：
 * 1.不比较msglen
 * 2.如果body中有任何一个为*，即不关心值，则bodylen也不关心
 */
public class Message {

    public Message(String p2pStr){
        mgr = new BufferMgr();
        config = U.getMsgConfig(p2pStr);
        initBody(p2pStr);
        initHeader(p2pStr);
        initLongHeader(p2pStr);
        initHttpHeader(p2pStr);
    }
    public Message(String p2pStr, byte[] data){
        mgr = new BufferMgr(data);
        config = U.getMsgConfig(p2pStr);
        initBody(p2pStr);
        initHeader(p2pStr);
        initLongHeader(p2pStr);
        initHttpHeader(p2pStr);
    }

    private void initBody(String p2pStr){
        String strSection = U.getBodySec(p2pStr);
        if(strSection == null || strSection.equals(""))
            msgBody = null;
        else{
            ArrayList<Field>  list = U.getFieldList(strSection, config);
            msgBody = new MsgBody(list, true);
        }
    }
    private void initHeader(String p2pStr){
        String strSection = U.getHeaderSec(p2pStr);
        if(strSection == null || strSection.equals(""))
            header = null;
        else{
            ArrayList<Field>  list = U.getFieldList(strSection, config);
            header = new MsgHeader(list, true);
            int len = 0;
            if(msgBody != null)
                len +=msgBody.getLen();
            header.setBodyLen(len);
        }
    }
    private void initLongHeader(String p2pStr){
        String strSection = U.getLHeaderSec(p2pStr);
        if(strSection == null || strSection.equals(""))
            longHeader = null;
        else{
            ArrayList<Field> list = U.getFieldList(strSection, config);
            longHeader = new MsgLongHeader(list, true);
            int len = 0;
            if(msgBody !=null) len += msgBody.getLen();
            if(header != null) len += header.getLen();
            longHeader.setBodyLen(len);
        }
    }

    private void initHttpHeader(String p2pStr){
        String strSection = U.getHttpHeaderSec(p2pStr);
        if(strSection == null || strSection.equals(""))
            http = null;
        else{
            if(strSection.equals("*"))
                http = new MsgHttpHeader(null, false);
            else
                http = new MsgHttpHeader(strSection, true);
            int len = 0;
            if(msgBody !=null) len += msgBody.getLen();
            if(header != null) len += header.getLen();
            if(longHeader != null) len += longHeader.getLen();
            if(len > 0)
                http.addBodyLen(len);
        }
    }

    public byte[] encode(){
        if(http != null)
            http.encode(mgr);
        if(longHeader != null)
            longHeader.encode(mgr);
        if(header != null)
            header.encode(mgr);
        if(msgBody != null)
            msgBody.encode(mgr);
        return mgr.getBuffer();
    }

    public void decode(){
        try{
            if(http != null)
                http.decode(mgr);
            if(longHeader != null)
                longHeader.decode(mgr);
            if(header != null)
                header.decode(mgr);
            if(msgBody != null)
                msgBody.decode(mgr);
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage() + "\r\n" + this.toString());
        }
    }

    public String toString(){
        String ret = "";
        if(http != null)
            ret += http;
        if(longHeader != null)
            ret += longHeader;
        if(header != null)
            ret += header;
        if(msgBody != null)
            ret += msgBody;
        return ret;
    }

    public CompareResult compare(Message other){
        if(other == null)
            return new CompareResult(false, "对象为空");
        if(this == other)
            return new CompareResult(true, "");
        CompareResult result = null;
        if(http != null){
            result = this.http.compare(other.http);
            if(!result.equal)
                return result;
        }
        if(longHeader != null){
            result = this.longHeader.compare(other.longHeader);
            if(!result.equal)
                return result;
        }
        if(this.header != null){
            result = this.header.compare(other.header);
            if(!result.equal)
                return result;
        }
        if(this.msgBody != null){
            result = this.msgBody.compare(other.msgBody);
            if(!result.equal)
                return result;
        }
        return  new CompareResult(true, "");
    }
    private MsgHttpHeader http;
    private MsgLongHeader longHeader;
    private MsgHeader header;
    private MsgBody msgBody;
    private BufferMgr mgr;
    private MsgConfig config;

    public static void main(String[] args) throws IOException {
        String input = "";
        String tmp;
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("d:\\test.txt")));
        while((tmp = in.readLine()) != null)
            input += tmp + "\r\n";


        //message.mgr.setBuffer(new byte[]{});
        //System.out.println(BU.bytes2HexGoodLook(message.encode()));
/*
        message.initLongHeader(input);
        message.longHeader.addBodyLen(128);
        message.longHeader.encode(message.mgr);
        message.initBody(input);
        message.msgBody.encode(message.mgr);*/
        Message message1 = new Message(input);
        System.out.println(BU.bytes2HexGoodLook(message1.encode()));
        Message msg = new Message(input, new byte[]{});
        msg.decode();
        System.out.println(BU.bytes2HexGoodLook(msg.mgr.getBuffer()));
        System.out.println(message1.compare(msg).equal);
/*
        Message message = new Message(input, BU.hex2Bytes("474554202f436f6e74656e742d4c656e6774683a2036340d0a0d0a00010000000200030000000000000004000000050000000000000006000000070000001c000000c80000000100000010f737b25be49bf056d18bd26524961d6d"));
        //message.decode();
        System.out.println("======================================");
        System.out.println(BU.bytes2HexGoodLook(message.mgr.getBuffer()));
        message.decode();
        System.out.println(BU.bytes2HexGoodLook(message.mgr.getBuffer()));


        CompareResult result = message1.compare(message);
        System.out.println(result.equal);
        System.out.println(result.msg);*/


    }
}