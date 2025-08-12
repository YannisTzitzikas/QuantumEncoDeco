package com.csd.stage;

import java.util.Map;
import javax.xml.bind.ValidationException;

import com.csd.common.params.ParameterDescriptor;
import com.csd.config.StageConfig;
import com.csd.core.stage.StageDescriptor;

public final class StageConfigBinder {

    // TODO(gtheo): Implement this, not Urgent
    public static Map<String, ParameterDescriptor> bind(StageConfig cfg, StageDescriptor profile) {
        Map<String, ParameterDescriptor> out = profile.getParams();
        return out;
    }

    public static void checkType(ParameterDescriptor pd, Object value) throws ValidationException {
        if (!pd.getType().rawType().isInstance(value)) {
            throw new ValidationException("Param '" + pd.getName() + "' type mismatch");
        }
    }

    public static void checkPredicate(ParameterDescriptor pd, Object value) throws ValidationException {
        if (!pd.getValidator().test(value)) {
            throw new ValidationException("Param '" + pd.getName() + "' failed validation");
        }
    }

}
