package io.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import com.codahale.metrics.httpclient.InstrumentedHttpRequestExecutor;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class CachingInstrumentedHttpRequestExecutor extends InstrumentedHttpRequestExecutor {
    private final MetricRegistry registry;
    private final HttpClientMetricNameStrategy metricNameStrategy;
    private final String name;

    public CachingInstrumentedHttpRequestExecutor(MetricRegistry registry,
                                                  HttpClientMetricNameStrategy metricNameStrategy) {
        this(registry, metricNameStrategy, null);
    }

    public CachingInstrumentedHttpRequestExecutor(MetricRegistry registry,
                                                  HttpClientMetricNameStrategy metricNameStrategy,
                                                  String name) {
        this(registry, metricNameStrategy, name, 3000);
    }

    public CachingInstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpClientMetricNameStrategy metricNameStrategy,
                                           String name,
                                           int waitForContinue) {
        super(registry, metricNameStrategy, name, waitForContinue);
        this.registry = registry;
        this.name = name;
        this.metricNameStrategy = metricNameStrategy;
    }

    @Override
    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context) throws HttpException, IOException {
        HttpResponse response = super.execute(request, conn, context);
        CacheResponseStatus responseStatus = ((HttpCacheContext) context).getCacheResponseStatus();
        String metricName = this.metricNameStrategy.getNameFor(this.name, request);
        this.registry.meter(MetricRegistry.name(metricName, responseStatus.toString().toLowerCase())).mark();
        return response;
    }
}
