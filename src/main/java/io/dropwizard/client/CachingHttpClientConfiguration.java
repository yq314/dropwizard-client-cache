package io.dropwizard.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.impl.client.cache.CacheConfig;

import javax.annotation.Nullable;
import javax.validation.Valid;

public class CachingHttpClientConfiguration extends HttpClientConfiguration {
    @Valid
    @Nullable
    private CacheConfig cacheConfig;

    @JsonProperty("cache")
    @Nullable
    public CacheConfig getCacheConfig() {
        return this.cacheConfig;
    }

    @JsonProperty("cache")
    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }
}
