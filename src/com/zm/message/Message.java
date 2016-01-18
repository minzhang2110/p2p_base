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
        encrypt();
        return mgr.getBuffer();
    }

    public void decode(){
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
                    throw new IllegalStateException("回包中不存在HTTP协议");
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
        if(header == null)
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
    private MsgHttpHeader http;
    private MsgLongHeader longHeader;
    private MsgHeader header;
    private MsgBody msgBody;
    private BufferMgr mgr;
    private MsgConfig config;

    public static void main(String[] args) throws IOException {
        /*
        String input = "";
        String tmp;
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("d:\\test.txt")));
        while((tmp = in.readLine()) != null)
            input += tmp + "\r\n";
        */
        /*
        String input = "[http]\n" +
                "POST / HTTP/1.1\n" +
                "\n" +
                "[b]\n" +
                "b@httpbody = 123123";
        Message msg = new Message(input);
        //System.out.println(BU.bytes2HexGoodLook(msg.encode()));
        System.out.println(new String(msg.encode()));*/
        String input = "[http]\n" +
                "*\n" +
                "\n" +
                "[b]\n" +
                "b@httpbody = *";
        byte[] buffer = ("POST / HTTP/1.1\r\n" +
                "Content-Length: 6\r\n" +
                "\r\n" +
                "123123").getBytes();
        Message msg = new Message(input, buffer);
        msg.decode();
        System.out.println(msg);
    }
}