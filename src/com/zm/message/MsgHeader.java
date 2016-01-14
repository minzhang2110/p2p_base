package com.zm.message;

import com.zm.Field.CompareResult;
import com.zm.Field.Field;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/22.
 */
public class MsgHeader extends MsgBlock {

    public MsgHeader(ArrayList<Field> list, boolean valueCare) {
        super(list, valueCare);
    }
}
