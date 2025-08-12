package com.csd.stage.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.csd.common.type.TypeRef;
import com.csd.stage.ParameterDescriptor;
import com.csd.stage.R1Stage;
import com.csd.stage.Stage;
import com.csd.stage.StageDescriptor;

public final class R2Provider implements StageProvider {

    private final StageDescriptor defaultProfile;

    public R2Provider() {
        TypeRef in  = TypeRef.parameterized(Iterable.class, TypeRef.Arg.bound());
        TypeRef out = TypeRef.simple(Integer.class);

        List<ParameterDescriptor> params = Arrays.asList(
            ParameterDescriptor.of("startOffset", TypeRef.simple(Integer.class))
                               .defaultValue(0)
                               .validate(o -> ((Integer)o) >= 0)
                               .build()
        );

        defaultProfile = new StageDescriptor(id(), in, out, params);
    }

    @Override
    public String id() { return "R2"; }

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
