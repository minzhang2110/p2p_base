package com.zm.message;

import com.zm.Field.CompareResult;
import com.zm.Field.Field;

import com.zm.Field.OneByte;
import com.zm.encryption.AESEncryptApplication;
import com.zm.encryption.Encrypt;
import com.zm.encryption.MHXY_UDP;
import com.zm.utils.BU;
import com.zm.utils.U;

import java.io.*;
import java.util.ArrayList;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/11/22.
 * 已知问题：
 * 一、在字段中有*时，预期的Content-Length，msglen，bodylen与实际可能不同
 * 现在的msglen和bodylen的值最好为*
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
            msgBody = new MsgBlock(list, true);
            //msgBody.setBodyLen(0);
        }
    }
    private void initHeader(String p2pStr){
        String strSection = U.getHeaderSec(p2pStr);
        if(strSection == null || strSection.equals(""))
            header = null;
        else{
            ArrayList<Field>  list = U.getFieldList(strSection, config);
            header = new MsgBlock(list, true);
            int len = 0;
            if(msgBody != null)
                len += msgBody.getLen();
            header.setBodyLen(len);
        }
    }
    private void initLongHeader(String p2pStr){
        String strSection = U.getLHeaderSec(p2pStr);
        if(strSection == null || strSection.equals(""))
            longHeader = null;
        else{
            ArrayList<Field> list = U.getFieldList(strSection, config);
            longHeader = new MsgBlock(list, true);
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

    //每个包只允许encode或decode一次
    public byte[] encode(){
        if(!encodedOrDecoded){
            if(http != null)
                http.encode(mgr);
            if(longHeader != null)
                longHeader.encode(mgr);
            if(header != null)
                header.encode(mgr);
            if(msgBody != null)
                msgBody.encode(mgr);
            encrypt();
            encodedOrDecoded = true;
        }
        return mgr.getBuffer();
    }

    //每个包只允许encode或decode一次
    public void decode(){
        if(!encodedOrDecoded){
            decrypt();
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
            encodedOrDecoded = true;
        }
    }

    private void encrypt(){
        if (config.encrypt != Encrypt.NONE){
            if(header == null && msgBody == null)//加密的部分是协议头和协议体，有长连接一般不加密，将长连接的设置len取消
                return;
            int msgLen = 0;
            if(header != null)
                msgLen += header.getLen();
            if(msgBody != null)
                msgLen +=msgBody.getLen();

            byte[] stay = BU.subByte(mgr.getBuffer(), 0, mgr.Length() - msgLen);
            byte[] before = BU.subByte(mgr.getBuffer(), mgr.Length() - msgLen, msgLen);
            byte[] after = null;
            switch (config.encrypt){
                case AES:
                    if(before.length < 12){
                        throw new IllegalStateException("AES加密失败,数据小于12字节");
                    }
                    if((after = AESEncryptApplication.encryptEx(before, before.length)) == null){
                        throw new IllegalStateException("AES加密失败");
                    }
                    else {
                        mgr = new BufferMgr();
                        mgr.putBuffer(BU.bytesMerger(stay, after));
                        setContentLength(after.length);
                    }
                    break;
                case MHXY:
                    if((after = MHXY_UDP.encrypt(1, before, before.length)) == null)
                        throw new IllegalStateException("mhxy_udp加密失败");
                    else {
                        mgr = new BufferMgr();
                        mgr.putBuffer(BU.bytesMerger(stay, after));
                        setContentLength(after.length);
                    }
                    break;
                default:
                    return;
            }
        }
    }

    private void decrypt(){
        if(config.encrypt != Encrypt.NONE){
            int index = 0;
            if(http != null){
                index += BU.findFirst(mgr.getBuffer(), "\r\n\r\n".getBytes());
                if(index == -1)
                    throw new IllegalStateException("包体中不存在HTTP协议头");
                index += 4;
            }

            byte[] stay = BU.subByte(mgr.getBuffer(), 0, index);
            byte[] before = BU.subByte(mgr.getBuffer(), index, mgr.Length() - index);
            byte[] after = null;
            switch (config.encrypt){
                case AES:
                    if(before.length < 12){
                        throw new IllegalStateException("AES解密失败,数据小于12字节");
                    }
                    if((after = AESEncryptApplication.decryptEx(before, before.length)) == null){
                        throw new IllegalStateException("AES解密失败");
                    }
                    else {
                        mgr = new BufferMgr();
                        mgr.putBuffer(BU.bytesMerger(stay, after));
                        setContentLength(after.length);
                    }
                    break;
                case MHXY:
                    if((after = MHXY_UDP.decrypt(before, before.length)) == null)
                        throw new IllegalStateException("mhxy_udp解密失败");
                    else {
                        mgr = new BufferMgr();
                        mgr.putBuffer(BU.bytesMerger(stay, after));
                        setContentLength(after.length);
                    }
                    break;
                default:
                    return;
            }
        }
    }

    private boolean setContentLength(int contentLength){
        if(http == null)
            return false;
        Pattern pattern = Pattern.compile("([\\s\\S]*Content-Length: )([0-9]+)");
        Matcher matcher = pattern.matcher(new String(mgr.getBuffer()));
        if(matcher.find()){
            byte[] part1 = matcher.group(1).getBytes();
            byte[] part2 = ("" + contentLength).getBytes();
            byte[] part3 = null;
            int index = BU.findFirst(mgr.getBuffer(), "\r\n\r\n".getBytes());
            if(index == -1)
                return false;
            else {
                part3 = BU.subByte(mgr.getBuffer(), index, mgr.Length() - index);
            }
            mgr = new BufferMgr();
            mgr.putBuffer(part1);
            mgr.putBuffer(part2);
            mgr.putBuffer(part3);
            return true;
        }
        return false;
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

    public int dataCntLeftToDecode(){
        return mgr.dataCntLeftToDecode();
    }

    public int getCmdID(){
        ArrayList<Field> list = null;
        Field field = null;
        if(header != null){
            list = header.list;
            for(int i = 0; i < list.size(); i++){
                field = list.get(i);
                if(field.getName().toLowerCase().equals("cmdid")
                        && field.getClass() == OneByte.class
                        && !field.getStrValue().equals(""))
                    return U.parseByte(field.getStrValue());
            }
        }

        if(msgBody != null){
            list = msgBody.list;
            for(int i = 0; i < list.size(); i++){
                field = list.get(i);
                if(field.getName().toLowerCase().equals("cmdid")
                        && field.getClass() == OneByte.class
                        && !field.getStrValue().equals(""))
                    return U.parseByte(field.getStrValue());
            }
        }

        if(longHeader != null){
            list = longHeader.list;
            for(int i = 0; i < list.size(); i++){
                field = list.get(i);
                if(field.getName().toLowerCase().equals("cmdid")
                        && field.getClass() == OneByte.class
                        && !field.getStrValue().equals(""))
                    return U.parseByte(field.getStrValue());
            }
        }
        return 0;
    }

    private MsgHttpHeader http;
    public  MsgBlock longHeader;
    public  MsgBlock header;
    public MsgBlock msgBody;
    private BufferMgr mgr;
    private MsgConfig config;
    private Boolean encodedOrDecoded = false;//编码或解码过了

    public static void main(String[] args) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("e://test.txt"))));
        char[] tmp = new char[4096];
        int len = reader.read(tmp);
        String input = new String(tmp, 0 ,len);

        Message msg = new Message(input);
        System.out.println(BU.bytes2HexGoodLook(msg.encode()));
        System.out.println(msg);

/*        Message msg2 = new Message(input, msg.encode());
        msg2.decode();
        System.out.println(msg2);

        System.out.println(msg.compare(msg2));*/



/*
        String input2 = "[h]\n" +
                "4@proversion = super.proversion\n" +
                "4@seqnum = super.seqnum\n" +
                "4@bodylen = *\n" +
                "1@cmdid = 99\n s@logdata = *";
        Message msg2 = new Message(input2, msg.encode());
        msg2.decode();
        //System.out.println(msg2);
        System.out.println(msg.getCmdID());
        System.out.println(msg2.getCmdID());*/


    }
}