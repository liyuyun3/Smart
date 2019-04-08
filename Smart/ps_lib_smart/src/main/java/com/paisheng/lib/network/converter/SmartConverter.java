package com.paisheng.lib.network.converter;

import java.lang.reflect.Type;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/11/4 16:34
 */
public abstract class SmartConverter<T> implements IConverter<T> {

    protected Type dataType;

    public SmartConverter(Type type) {
        this.dataType = type;
    }

    public void setType(Type type) {
        this.dataType = type;
    }

}
