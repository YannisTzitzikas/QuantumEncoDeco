package com.csd.core;

import java.util.concurrent.ExecutorService;

import com.csd.config.Config;
import com.csd.core.storage.StorageEngine;

public final class PipelineContext {

    // FIXME(gtheo): Remove config, exchange with something else, will see later on
    final Config config; 
    final StorageEngine storage;
    final ExecutorService executor;

    // Flesh out later
    PipelineContext(Config cfg){
        this.config = cfg;
        this.storage = null;
        this.executor = null;
    }
}