package com.csd.stage.descriptor;

import java.util.Arrays;

import com.csd.common.params.ParameterDescriptor;
import com.csd.common.type.TypeRef;
import com.csd.core.model.uri.URITriple;
import com.csd.core.stage.StageDescriptor;

public final class URIBatchReaderDescriptor extends StageDescriptor {
    public URIBatchReaderDescriptor() {
        super(
            "URIBatchReader",
            TypeRef.parameterized(String.class),
            TypeRef.parameterized(Iterable.class, TypeRef.Arg.of(URITriple.class)),
            Arrays.asList(
                ParameterDescriptor.of("batchSize", TypeRef.simple(Integer.class))
                                   .defaultValue(0)
                                   .validate(o -> ((Integer) o) >= 0 && ((Integer) o) <= 10_000_000)
                                   .build()
            )
        );
    }
}
