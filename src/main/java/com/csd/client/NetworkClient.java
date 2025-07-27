package com.csd.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.csd.client.model.JobResult;
import com.csd.client.model.JobStatus;
import com.csd.client.model.Mapping;
import com.csd.client.model.ServerMetadata;
import com.csd.core.config.Config;
import com.google.gson.Gson;

public class NetworkClient implements ClientAPI {
    private final CloseableHttpClient httpClient;
    private final String serverBaseUrl;
    private final ExecutorService callbackExecutor;
    private final Gson gson = new Gson();

    public NetworkClient(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
        this.httpClient = HttpClients.createDefault();
        this.callbackExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public CompletableFuture<JobResult> encode(Config config) {
        return sendJobRequest("encode", config);
    }

    @Override
    public CompletableFuture<JobResult> decode(Config config) {
        return sendJobRequest("decode", config);
    }

    private CompletableFuture<JobResult> sendJobRequest(String endpoint, Config config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = gson.toJson(config);
                HttpPost request = new HttpPost(serverBaseUrl + "/" + endpoint);
                request.setHeader("Content-Type", "application/json");
                request.setEntity(new StringEntity(json));

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int status = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (status >= 400) {
                        throw new ClientException("Server error: " + responseBody);
                    }

                    return gson.fromJson(responseBody, JobResult.class);
                }
            } catch (Exception e) {
                throw new ClientException("Request failed", e);
            }
        }, callbackExecutor);
    }

    @Override
    public CompletableFuture<Mapping> downloadMappings(String mappingId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(serverBaseUrl + "/mappings/" + mappingId);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int status = response.getStatusLine().getStatusCode();
                    byte[] data = EntityUtils.toByteArray(response.getEntity());

                    if (status != 200) {
                        throw new ClientException("Failed to download mappings: " + status);
                    }

                    return new Mapping(mappingId, data);
                }
            } catch (Exception e) {
                throw new ClientException("Download failed", e);
            }
        }, callbackExecutor);
    }

    @Override
    public CompletableFuture<ServerMetadata> fetchMetadata() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(serverBaseUrl + "/metadata");
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String json = EntityUtils.toString(response.getEntity());
                    return gson.fromJson(json, ServerMetadata.class);
                }
            } catch (Exception e) {
                throw new ClientException("Metadata fetch failed", e);
            }
        }, callbackExecutor);
    }

    @Override
    public CompletableFuture<JobStatus> getJobStatus(String jobId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(serverBaseUrl + "/jobs/" + jobId);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String json = EntityUtils.toString(response.getEntity());
                    return gson.fromJson(json, JobStatus.class);
                }
            } catch (Exception e) {
                throw new ClientException("Status check failed", e);
            }
        }, callbackExecutor);
    }

    public void shutdown() {
        callbackExecutor.shutdown();
    }
}