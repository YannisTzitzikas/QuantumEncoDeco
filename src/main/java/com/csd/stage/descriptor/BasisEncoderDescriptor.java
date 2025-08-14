package com.csd.stage.descriptor;

import java.util.Arrays;
import java.util.Map;

import com.csd.common.params.ParameterDescriptor;
import com.csd.common.type.TypeRef;
import com.csd.core.stage.StageDescriptor;

public final class BasisEncoderDescriptor extends StageDescriptor {
    public BasisEncoderDescriptor() {
        super(
            "BasisEncoder",
            TypeRef.parameterized(Iterable.class, TypeRef.Arg.bound()),
            TypeRef.parameterized(Map.class, TypeRef.Arg.bound(), TypeRef.Arg.of(Integer.class)),
            Arrays.asList(
                ParameterDescriptor.of("startOffset", TypeRef.simple(Integer.class))
                                   .defaultValue(0)
                                   .validate(o -> ((Integer) o) >= 0)
                                   .build()
            )
        );
    }
}
