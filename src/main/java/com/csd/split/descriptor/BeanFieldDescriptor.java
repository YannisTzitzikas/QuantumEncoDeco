package com.csd.split.descriptor;

import com.csd.common.type.TypeRef;
import com.csd.core.split.SplitterDescriptor;

public final class BeanFieldDescriptor extends SplitterDescriptor {
    public BeanFieldDescriptor()
    {
        super(TypeRef.bound(),TypeRef.bound(),false,null);
    }
}
