package com.zm.Field;

/**
 * Created by Administrator on 2015/11/22.
 */
public class CompareResult {
    public CompareResult(boolean equal, String msg) {
        this.equal = equal;
        this.msg = msg;
    }

    public String toString(){
        return msg;
    }

    public boolean equal = false;
    public String msg = "";
}
