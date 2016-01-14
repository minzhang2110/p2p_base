package com.zm.message;

import com.zm.Field.CompareResult;
import com.zm.Field.Field;
import com.zm.Field.OneByte;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/22.
 */
public class MsgBody extends MsgBlock {

    public MsgBody(ArrayList<Field> list, boolean valueCare) {
        super(list, valueCare);
    }
}
