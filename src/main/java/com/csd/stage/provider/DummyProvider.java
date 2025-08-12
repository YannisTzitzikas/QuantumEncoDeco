package com.csd.stage.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.csd.common.params.ParameterDescriptor;
import com.csd.common.type.TypeRef;
import com.csd.core.stage.Stage;
import com.csd.core.stage.StageDescriptor;
import com.csd.stage.R1Stage;

public class DummyProvider implements StageProvider {
    
    private final StageDescriptor defaultProfile;
    
    public DummyProvider() {
        TypeRef in  = TypeRef.parameterized(String.class);
        TypeRef out = TypeRef.parameterized(Iterable.class, TypeRef.Arg.of(TypeRef.simple(String.class)));

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
        return "Dummy";
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
