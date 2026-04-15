package org.amupoti.sm.main.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.amupoti.supermanager.parser.acb.teams.SMUserTeamService;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Marcel on 06/08/2015.
 */
@Configuration
@PropertySource("classpath:urls.properties")
@ComponentScan("org.amupoti.supermanager.parser.rdm")
@Slf4j
public class ApplicationConfig {

    @Value("${http.connect-timeout-ms:30000}")
    private int connectTimeoutMs;

    @Value("${http.request-timeout-ms:30000}")
    private int requestTimeoutMs;

    @Value("${http.socket-timeout-ms:60000}")
    private int socketTimeoutMs;

    @Autowired
    CloseableHttpClient httpClient;

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                caffeineCache("TeamData",    Duration.ofHours(25)),
                caffeineCache("NextMatch",   Duration.ofHours(6)),
                caffeineCache("RdmTeamData", Duration.ofHours(25)),
                caffeineCache("teamsPage",   Duration.ofMinutes(30)),
                caffeineCache("marketPage",  Duration.ofMinutes(30))
        ));
        return manager;
    }

    private CaffeineCache caffeineCache(String name, Duration ttl) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .build());
    }

    /** Bounded pool for parallel ACB API calls (teams list, market, roster pages). */
    @Bean(name = "acbFetchExecutor")
    public ExecutorService acbFetchExecutor() {
        return Executors.newFixedThreadPool(5);
    }

    /** Bounded pool for parallel RDM pre-warm in the background refresh job. */
    @Bean(name = "rdmFetchExecutor")
    public ExecutorService rdmFetchExecutor() {
        return Executors.newFixedThreadPool(3);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient());
        return clientHttpRequestFactory;
    }

    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(requestTimeoutMs))
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
                .setResponseTimeout(Timeout.ofMilliseconds(socketTimeoutMs))
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.ZERO_MILLISECONDS) {
                    @Override
                    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
                        if (exception instanceof org.apache.hc.core5.http.NoHttpResponseException) {
                            log.warn("No response from server on {} call", execCount);
                            return execCount <= 3;
                        }
                        if (execCount > 3) {
                            log.warn("Maximum tries reached for client http pool");
                        }
                        return false;
                    }
                })
                .build();
    }

    @Bean
    public SMUserTeamService getAcbTeamService() {
        return new SMUserTeamService(getSmContentProvider(), getSmContentParser(), acbFetchExecutor());
    }

    @Bean
    public SmContentParser getSmContentParser() {
        return new SmContentParser();
    }

    @Bean
    public SmContentProvider getSmContentProvider() {
        return new SmContentProvider();
    }
}
