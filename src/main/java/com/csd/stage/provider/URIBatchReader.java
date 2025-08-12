package com.csd.stage.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.csd.common.type.TypeRef;
import com.csd.stage.ParameterDescriptor;
import com.csd.stage.R1Stage;
import com.csd.stage.Stage;
import com.csd.stage.StageDescriptor;

import com.csd.core.model.uri.URITriple;

public class URIBatchReader implements StageProvider {
    
    private final StageDescriptor defaultProfile;
    
    public URIBatchReader() {
        TypeRef in  = TypeRef.parameterized(String.class);
        TypeRef out = TypeRef.parameterized(Iterable.class, TypeRef.Arg.of(TypeRef.simple(URITriple.class)));

        List<ParameterDescriptor> params = Arrays.asList(
            ParameterDescriptor.of("batchSize", TypeRef.simple(Integer.class))
                               .defaultValue(0)
                               .validate(o -> ((Integer)o) >= 0 && ((Integer)o) <= 10_000_000)
                               .build()
        );

        defaultProfile = new StageDescriptor(id(), in, out, params);
    }


    @Override
    public String id() {
        return "URIBatchReader";
    }

    @Override
    public StageDescriptor defaultProfile() {
        return defaultProfile;
    }

    @Override
    public Stage create(Map<String, ParameterDescriptor> params) throws Exception {
        return new R1Stage(0, defaultProfile);
    }

}
