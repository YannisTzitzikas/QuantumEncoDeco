package com.csd.stage.descriptor;

import java.util.Arrays;
import java.util.List;

import com.csd.common.params.ParameterDescriptor;
import com.csd.common.type.TypeRef;
import com.csd.core.stage.StageDescriptor;

public final class DummyDescriptor extends StageDescriptor {

    public DummyDescriptor() {
        super(buildId(), buildInputType(), buildOutputType(), buildParams());
    }
    
    private static String buildId() {
        return "dummy";
    }

    private static TypeRef buildInputType() {
        return TypeRef.parameterized(String.class);
    }

    private static TypeRef buildOutputType() {
        return TypeRef.parameterized(Iterable.class, TypeRef.Arg.of(String.class));
    }

    private static List<ParameterDescriptor> buildParams() {
        
        List<ParameterDescriptor> params = Arrays.asList(
            ParameterDescriptor.of("batchSize", TypeRef.simple(Integer.class))
                .defaultValue(0)
                .validate(o -> ((Integer) o) >= 0 && ((Integer) o) <= 10_000_000)
                .build()
        );
        
        return params;
    }
}

