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
            throw new IllegalArgumentException("消息体不能为空");
        else{
            if(strSection.equals("*"))
                msgBody = new MsgBody(null, false);
            else{
                ArrayList<Field>  list = U.getFieldList(strSection, config);
                msgBody = new MsgBody(list, true);
            }
        }
    }
    private void initHeader(String p2pStr){
        String strSection = U.getHeaderSec(p2pStr);
        if(strSection == null || strSection.equals(""))
            throw new IllegalArgumentException("协议头不能为空");
        else{
            ArrayList<Field> list = U.getFieldList(strSection, config);
            if(list.size() == 2){
                header = new MsgHeaderInter(list, true);
                header.addBodyLen(msgBody.getLen());
            }
            else if(list.size() == 1)
                header = new MsgHeaderUDP(list, true);
            else throw new IllegalArgumentException("个数为" + list.size()+"的协议头不存在");
        }

    }
    private void initLongHeader(String p2pStr){
        String strSection = U.getLHeaderSec(p2pStr);
        if(strSection == null || strSection.equals(""))
            longHeader = null;
        else{
            if(strSection.equals("*"))
                longHeader = new MsgLongHeader(null, false);
            else{
                ArrayList<Field> list = U.getFieldList(strSection, config);
                longHeader = new MsgLongHeader(list, true);
            }
            int len = 0;
            if(msgBody !=null) len += msgBody.getLen();
            if(header != null) len += header.getLen();
            longHeader.addBodyLen(len);
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
        if(http != null)
            http.decode(mgr);
        if(longHeader != null)
            longHeader.decode(mgr);
        if(header != null)
            header.decode(mgr);
        if(msgBody != null)
            msgBody.decode(mgr);
    }

    private void encrypt(){
        int msgLen = header.getLen() + msgBody.getLen();
        byte[] stay = BU.subByte(mgr.getBuffer(), 0, mgr.Length() - msgLen);
        byte[] before = BU.subByte(mgr.getBuffer(), mgr.Length() - msgLen, msgLen);
        byte[] after = null;
        switch (config.encrypt){
            case NONE:
                break;
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
                    setMsgLen(after.length);
                    int len = 0;
                    if(longHeader != null)
                        len += longHeader.getLen();
                    setContentLength(after.length + len);
                }
                break;
            case MHXY:
                if((after = MHXY_UDP.encrypt(1, before, before.length)) == null)
                    throw new IllegalStateException("mhxy_udp加密失败");
                else {
                    mgr = new BufferMgr();
                    mgr.putBuffer(BU.bytesMerger(stay, after));
                    setMsgLen(after.length);
                    setContentLength(after.length + longHeader.getLen());
                }
                break;
            default:
                return;
        }
    }


    private void decrypt(){
        int index = 0;
        if(http != null){
            index += BU.find(mgr.getBuffer(), "\r\n\r\n".getBytes());
        }
        if(longHeader != null){
            index += 4 + 36;
        }
        byte[] stay = BU.subByte(mgr.getBuffer(), 0, index);
        byte[] before = BU.subByte(mgr.getBuffer(), index, mgr.Length() - index);
        byte[] after = null;
        switch (config.encrypt){
            case NONE:
                break;
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
                    setMsgLen(after.length);
                    int len = 0;
                    if(longHeader != null)
                        len += longHeader.getLen();
                    setContentLength(after.length + len);
                }
                break;
            case MHXY:
                if((after = MHXY_UDP.decrypt(before, before.length)) == null)
                    throw new IllegalStateException("mhxy_udp解密失败");
                else {
                    mgr = new BufferMgr();
                    mgr.putBuffer(BU.bytesMerger(stay, after));
                    setMsgLen(after.length);
                    setContentLength(after.length + longHeader.getLen());
                }
                break;
            default:
                return;
        }
    }

    private boolean setMsgLen(int msgLen){
        if(longHeader == null)
            return false;
        int index = 0;
        if(http != null)
            index = BU.find(mgr.getBuffer(), "\r\n\r\n".getBytes());
        index += 4 + 32;
        byte[] part1 = BU.subByte(mgr.getBuffer(), 0, index);
        byte[] part3 = BU.subByte(mgr.getBuffer(), index + 4, mgr.Length() - index - 4);
        mgr = new BufferMgr();
        mgr.putBuffer(part1);
        new FourBytes("", "" + msgLen).encode(mgr);
        mgr.putBuffer(part3);
        return true;
    }

    private boolean setContentLength(int contentLength){
        if(header == null)
            return false;
        Pattern pattern = Pattern.compile("(.*Content-Length: )([0-9]+)");
        Matcher matcher = pattern.matcher(new String(mgr.getBuffer()));
        if(matcher.find()){
            byte[] part1 = matcher.group(1).getBytes();
            byte[] part2 = ("" + contentLength).getBytes();
            byte[] part3 = null;
            int index = BU.find(mgr.getBuffer(), "\r\n\r\n".getBytes());
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

    public CompareResult compare(Message other){
        if(other == null)
            return new CompareResult(false, "对象为空");
        if(this == other)
            return new CompareResult(true, "");
        CompareResult result = null;
        result = this.http.compare(other.http);
        if(!result.equal)
            return result;
        result = this.longHeader.compare(other.longHeader);
        if(!result.equal)
            return result;
        result = this.header.compare(other.header);
        if(!result.equal)
            return result;
        result = this.msgBody.compare(other.msgBody);
        if(!result.equal)
            return result;
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
        System.out.println(BU.bytes2HexGoodLook(message1.mgr.getBuffer()));

        Message message = new Message(input, BU.hex2Bytes("474554202f436f6e74656e742d4c656e6774683a2036340d0a0d0a00010000000200030000000000000004000000050000000000000006000000070000001c000000c80000000100000010f737b25be49bf056d18bd26524961d6d"));
        //message.decode();
        System.out.println("======================================");
        System.out.println(BU.bytes2HexGoodLook(message.mgr.getBuffer()));
        message.decode();
        System.out.println(BU.bytes2HexGoodLook(message.mgr.getBuffer()));


        CompareResult result = message1.compare(message);
        System.out.println(result.equal);
        System.out.println(result.msg);


    }
}