package io.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CachingInstrumentedHttpRequestExecutorTest {
    private CachingInstrumentedHttpRequestExecutor executor;
    private MetricRegistry metricRegistry;

    @BeforeEach
    public void setup() {
        metricRegistry = new MetricRegistry();
        executor = new CachingInstrumentedHttpRequestExecutor(metricRegistry, HttpClientMetricNameStrategies.METHOD_ONLY);
    }

    @Test
    public void canEmitMetricsForCacheResponseStatus() throws IOException, HttpException {
        HttpRequest request = new HttpGet("http://www.example.com");
        HttpClientConnection conn = mock(HttpClientConnection.class);
        HttpCacheContext context = mock(HttpCacheContext.class);

        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);

        when(statusLine.getStatusCode()).thenReturn(200);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(conn.receiveResponseHeader()).thenReturn(response);
        when(context.getCacheResponseStatus()).thenReturn(CacheResponseStatus.CACHE_HIT);
        executor.execute(request, conn, context);

        long count = metricRegistry
                .getMeters()
                .get("org.apache.http.client.HttpClient.get-requests.cache_hit")
                .getCount();

        assertThat(count).isEqualTo(1);
    }
}
