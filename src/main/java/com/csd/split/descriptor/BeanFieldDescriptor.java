package com.csd.split.descriptor;

import java.util.Collections;
import java.util.List;

import com.csd.common.reflection.AccessStep;
import com.csd.common.reflection.ReflectionPathResolver;
import com.csd.common.type.TypeRef;
import com.csd.common.type.TypeRefUtils;
import com.csd.core.split.SplitterDescriptor;

public final class BeanFieldDescriptor extends SplitterDescriptor {
    public BeanFieldDescriptor()
    {
        super("BeanField", TypeRef.bound(),TypeRef.bound(),false, Collections.emptyList());
    }

    @Override
    public TypeRef projectTypeOutput(TypeRef passedInput, String param) {
        if (!TypeRefUtils.isConcrete(passedInput) ||
            !TypeRefUtils.isAssignable(passedInput, getInputType())) return null;

        TypeRef inputProjection = TypeRefUtils.GCType(passedInput, getInputType());
        if (inputProjection == null) return null;

        Class<?> rawClass = inputProjection.rawType();
        ReflectionPathResolver resolver = new ReflectionPathResolver(true);
        List<AccessStep> steps;
        try {
            steps = resolver.resolveChain(rawClass, param);
        } catch (Exception e) {
            return null;
        }

        TypeRef current = inputProjection;
        for (AccessStep step : steps) {
            TypeRef nextType = TypeRef.simple(step.getResultType());
            current = TypeRefUtils.GCType(current, nextType);
            if (current == null) return null;
        }
    
        return current;
    }
}
