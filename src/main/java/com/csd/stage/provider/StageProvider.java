package com.csd.stage.provider;

import java.util.Map;

import com.csd.stage.ParameterDescriptor;
import com.csd.stage.Stage;
import com.csd.stage.StageProfile;

public interface StageProvider {
    String id();
    StageProfile defaultProfile();
    Stage create(Map<String, ParameterDescriptor> params) throws Exception;
}
