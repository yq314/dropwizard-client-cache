package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.InstrumentedHttpClientConnectionManager;
import io.dropwizard.metrics.CachingInstrumentedHttpRequestExecutor;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

public class CachingHttpClientBuilderTest {
    private CachingHttpClientBuilder builder;

    @BeforeEach
    public void setUp() {
        builder = new CachingHttpClientBuilder(new MetricRegistry());
    }

    @Test
    public void canCreateClient() {
        CloseableHttpClient client = builder.build("test client");
        assertThat(client).isNotNull();
    }

    @Test
    public void canUseCustomConfig() throws IllegalAccessException {
        CacheConfig cacheConfig = CacheConfig.DEFAULT;

        CachingHttpClientBuilder returnedBuilder = builder.setCacheConfig(cacheConfig);
        assertThat(returnedBuilder).isSameAs(builder);

        final Field configField = FieldUtils
                .getField(builder.getClass(), "cacheConfig", true);
        assertThat(configField.get(builder)).isSameAs(cacheConfig);
    }

    @Test
    public void requestExecutorIsCachingInstrumented() throws IllegalAccessException {
        final String clientName = "test client";

        org.apache.http.impl.client.HttpClientBuilder httpClientBuilder =
                org.apache.http.impl.client.HttpClientBuilder.create();

        Registry configuredRegistry = RegistryBuilder.create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();

        InstrumentedHttpClientConnectionManager connectionManager = builder
                .createConnectionManager(configuredRegistry, clientName);

        builder.createClient(httpClientBuilder, connectionManager, clientName);

        final Field requestExecutorField = FieldUtils
                .getField(httpClientBuilder.getClass(), "requestExec", true);
        assertThat(requestExecutorField.get(httpClientBuilder)).isInstanceOf(CachingInstrumentedHttpRequestExecutor.class);
    }

    @Test
    public void returnCachingClientBuilder() throws IllegalAccessException {
        org.apache.http.impl.client.HttpClientBuilder httpClientBuilder = builder.createBuilder();
        assertThat(httpClientBuilder).isInstanceOf(org.apache.http.impl.client.cache.CachingHttpClientBuilder.class);

        final Field cacheConfigField = FieldUtils
                .getField(httpClientBuilder.getClass(), "cacheConfig", true);
        assertThat(cacheConfigField.get(httpClientBuilder)).isSameAs(CacheConfig.DEFAULT);
    }
}