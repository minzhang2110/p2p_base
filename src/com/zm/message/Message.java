package com.zm.message;

import com.sun.xml.internal.messaging.saaj.packaging.mime.Header;
import com.zm.Field.CompareResult;
import com.zm.Field.Field;
import com.zm.encryption.AESEncryptApplication;
import com.zm.encryption.Encrypt;
import com.zm.encryption.MHXY_UDP;
import com.zm.utils.BU;
import com.zm.utils.U;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/22.
 */
public class Message {

    public Message(String p2pStr){
        mgr = new BufferMgr();
        config = U.getMsgConfig(p2pStr);
        initHttpHeader(p2pStr);
        initBody(p2pStr);
        initHeader(p2pStr);
        initLongHeader(p2pStr);
    }
    public Message(String p2pStr, byte[] data){
        mgr = new BufferMgr(data);
        config = U.getMsgConfig(p2pStr);
        initHttpHeader(p2pStr);
        initBody(p2pStr);
        initHeader(p2pStr);
        initLongHeader(p2pStr);
    }

    private void initHttpHeader(String p2pStr){
        String strSection = U.getHttpHeaderSec(p2pStr);
        if(strSection == null || strSection.equals(""))
            http = null;
        else if(strSection.equals("*"))
            http = new MsgHttpHeader(null, false);
        else
            http = new MsgHttpHeader(strSection, true);
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
            longHeader.addBodyLen(msgBody.getLen() + header.getLen());
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
        //encrypt();
        return mgr.getBuffer();
    }

    public void decode(){
        //decrypt();
        if(http != null)
            http.decode(mgr);
        if(longHeader != null)
            longHeader.decode(mgr);
        if(header != null)
            header.decode(mgr);
        if(msgBody != null)
            msgBody.decode(mgr);
    }

    public void encrypt(){
        int msgLen = header.getLen() + msgBody.getLen();
        byte[] stay = BU.subByte(mgr.getBuffer(), 0, mgr.Length() - msgLen);
        byte[] before = BU.subByte(mgr.getBuffer(), mgr.Length() - msgLen, msgLen);
        byte[] after = null;
        switch (config.encrypt){
            case NONE:
                break;
            case AES:
                if((after = AESEncryptApplication.encryptEx(before, before.length)) == null)
                    throw new IllegalStateException("AES加密失败");
                mgr.putBuffer(BU.bytesMerger(stay, after));
                break;
            case MHXY:
                if((after = MHXY_UDP.encrypt(1, before, before.length)) == null)
                    throw new IllegalStateException("mhxy_udp加密失败");
                mgr.putBuffer(BU.bytesMerger(stay, after));
                break;
            default:
                return;
        }
    }

    public void decrypt(){

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

        Message message = new Message(input, BU.hex2Bytes("474554202f3132330d0a0d0a00000101010100020000000000000003000000040000000000000005000000060000000d000000c8000000010000000102"));
        message.decode();
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

        CompareResult result = message.compare(message1);
        System.out.println(result.equal);
        System.out.println(result.msg);
    }
}