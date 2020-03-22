package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import io.dropwizard.metrics.CachingInstrumentedHttpRequestExecutor;
import io.dropwizard.setup.Environment;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.protocol.HttpRequestExecutor;

public class CachingHttpClientBuilder extends HttpClientBuilder {
    private final MetricRegistry metricRegistry;
    private CacheConfig cacheConfig = CacheConfig.DEFAULT;
    private HttpClientMetricNameStrategy metricNameStrategy = HttpClientMetricNameStrategies.METHOD_ONLY;

    public CachingHttpClientBuilder(MetricRegistry metricRegistry) {
        super(metricRegistry);
        this.metricRegistry = metricRegistry;
    }

    public CachingHttpClientBuilder(Environment environment) {
        super(environment);
        this.metricRegistry = environment.metrics();
    }

    public CachingHttpClientBuilder using(CachingHttpClientConfiguration configuration) {
        super.using(configuration);
        this.cacheConfig = configuration.getCacheConfig();
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
