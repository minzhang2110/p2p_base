package com.zm.message;

import com.zm.Field.Order;
import com.zm.encryption.Encrypt;

/**
 * Created by Administrator on 2015/11/22.
 */
public class MsgConfig{
    @Override
    public String toString(){
        return ("加密:" + encrypt + "\t\t字节序:" + order);
    }
    public Encrypt encrypt = Encrypt.NONE;
    public Order order = Order.NET;
}