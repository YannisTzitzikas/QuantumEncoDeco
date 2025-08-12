package com.csd.stage.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.csd.common.params.ParameterDescriptor;
import com.csd.common.type.TypeRef;
import com.csd.core.stage.Stage;
import com.csd.core.stage.StageDescriptor;
import com.csd.stage.R1Stage;

public final class R1Provider implements StageProvider {

    private final StageDescriptor defaultProfile;

    public R1Provider() {
        TypeRef in  = TypeRef.parameterized(Iterable.class, TypeRef.Arg.bound());
        TypeRef out = TypeRef.parameterized(Map.class, TypeRef.Arg.bound(), TypeRef.Arg.of(TypeRef.simple(Integer.class)));

        List<ParameterDescriptor> params = Arrays.asList(
            ParameterDescriptor.of("startOffset", TypeRef.simple(Integer.class))
                               .defaultValue(0)
                               .validate(o -> ((Integer)o) >= 0)
                               .build()
        );

        defaultProfile = new StageDescriptor(id(), in, out, params);
    }

    @Override
    public String id() { return "R1"; }

    @Override
    public StageDescriptor defaultProfile() {
        return defaultProfile;
    }

    // FIXME(gtheo): Make it more abstract, make a factory probably, I am not sure. This will do for now to do some tests
    @Override
    public Stage create(Map<String, ParameterDescriptor> params) throws Exception {
        return new R1Stage(0, defaultProfile);
    }
}
