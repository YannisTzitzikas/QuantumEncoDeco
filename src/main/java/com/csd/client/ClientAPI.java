package com.csd.client;

import java.util.concurrent.CompletableFuture;

import com.csd.client.model.JobResult;
import com.csd.client.model.Mapping;
import com.csd.client.model.ServerMetadata;
import com.csd.client.model.JobStatus;

import com.csd.core.config.Config;

public interface ClientAPI {
    CompletableFuture<JobResult>        encode(Config config);
    CompletableFuture<JobResult>        decode(Config config);
    CompletableFuture<Mapping>          downloadMappings(String mappingId);
    CompletableFuture<ServerMetadata>   fetchMetadata();
    CompletableFuture<JobStatus>        getJobStatus(String jobId);
}