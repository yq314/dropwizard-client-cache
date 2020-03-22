package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.InstrumentedHttpClientConnectionManager;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.metrics.CachingInstrumentedHttpRequestExecutor;
import io.dropwizard.setup.Environment;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void managedByEnvironment() throws Exception {
        final Environment environment = mock(Environment.class);
        when(environment.getName()).thenReturn("test-env");
        when(environment.metrics()).thenReturn(new MetricRegistry());

        final LifecycleEnvironment lifecycle = mock(LifecycleEnvironment.class);
        when(environment.lifecycle()).thenReturn(lifecycle);

        final CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CachingHttpClientBuilder httpClientBuilder = spy(new CachingHttpClientBuilder(environment));
        when(httpClientBuilder.buildWithDefaultRequestConfiguration("test-apache-client"))
                .thenReturn(new ConfiguredCloseableHttpClient(httpClient, RequestConfig.DEFAULT));
        assertThat(httpClientBuilder.build("test-apache-client")).isSameAs(httpClient);

        // Verify that we registered the managed object
        final ArgumentCaptor<Managed> argumentCaptor = ArgumentCaptor.forClass(Managed.class);
        verify(lifecycle).manage(argumentCaptor.capture());

        // Verify that the managed object actually stops the HTTP client
        final Managed managed = argumentCaptor.getValue();
        managed.stop();
        verify(httpClient).close();
    }

    @Test
    public void canUseCustomConfig() throws IllegalAccessException {
        CacheConfig cacheConfig = CacheConfig.DEFAULT;
        CachingHttpClientConfiguration configuration = new CachingHttpClientConfiguration();
        configuration.setCacheConfig(cacheConfig);

        CachingHttpClientBuilder returnedBuilder = builder.using(configuration);
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

        Registry<ConnectionSocketFactory> configuredRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
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