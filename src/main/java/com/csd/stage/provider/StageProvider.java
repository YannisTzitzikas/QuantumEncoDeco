package com.csd.stage.provider;

import java.util.Map;

import com.csd.stage.ParameterDescriptor;
import com.csd.stage.Stage;
import com.csd.stage.StageDescriptor;

public interface StageProvider {
    StageDescriptor defaultProfile();
    Stage create(Map<String, ParameterDescriptor> params) throws Exception;
    
    // I am not sure about that.
    default String id() { return this.getClass().getSimpleName(); }
}
