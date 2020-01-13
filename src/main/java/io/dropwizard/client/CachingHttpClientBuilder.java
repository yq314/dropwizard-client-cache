package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import io.dropwizard.metrics.CachingInstrumentedHttpRequestExecutor;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.protocol.HttpRequestExecutor;

public class CachingHttpClientBuilder extends HttpClientBuilder {
    private final MetricRegistry metricRegistry;
    private CacheConfig cacheConfig;
    private HttpClientMetricNameStrategy metricNameStrategy;

    public CachingHttpClientBuilder(MetricRegistry metricRegistry) {
        super(metricRegistry);
        this.metricRegistry = metricRegistry;
        cacheConfig = CacheConfig.DEFAULT;
        metricNameStrategy = HttpClientMetricNameStrategies.METHOD_ONLY;
    }

    public CachingHttpClientBuilder setCacheConfig(CacheConfig config) {
        cacheConfig = config;
        return this;
    }

    @Override
    protected HttpRequestExecutor createRequestExecutor(String name) {
        return new CachingInstrumentedHttpRequestExecutor(metricRegistry, metricNameStrategy, name);
    }

    @Override
    protected org.apache.http.impl.client.HttpClientBuilder createBuilder() {
        org.apache.http.impl.client.cache.CachingHttpClientBuilder builder =
                org.apache.http.impl.client.cache.CachingHttpClientBuilder.create();
        builder.setCacheConfig(cacheConfig);
        return builder;
    }
}
