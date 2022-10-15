package org.amupoti.sm.main.config;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.amupoti.supermanager.parser.acb.teams.SMUserTeamService;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.amupoti.supermanager.parser.acb.SmContentProvider.ACTIVE_COMPETITION;

/**
 * Created by Marcel on 06/08/2015.
 */
@Configuration
@PropertySource("classpath:urls.properties")
@ComponentScan("org.amupoti.supermanager.parser.rdm")
@Slf4j
public class ApplicationConfig {

    private static final int CONNECT_TIMEOUT = 30 * 1000;
    private static final int REQUEST_TIMEOUT = 30 * 1000;
    private static final int SOCKET_TIMEOUT = 60 * 1000;

    @Autowired
    CloseableHttpClient httpClient;

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
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
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT).build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler((exception, executionCount, context) -> {
                    if (executionCount > 3) {
                        log.warn("Maximum tries reached for client http pool ");
                        return false;
                    }
                    if (exception instanceof org.apache.http.NoHttpResponseException) {
                        log.warn("No response from server on " + executionCount + " call");
                        return true;
                    }
                    return false;
                })
                .build();
    }

    @Bean
    public SMUserTeamService getAcbTeamService() {
        return new SMUserTeamService(getSmContentProvider(), getSmContentParser());
    }

    @Bean
    public SmContentParser getSmContentParser() {
        return new SmContentParser();
    }

    @Bean
    public SmContentProvider getSmContentProvider() {
        return new SmContentProvider(ACTIVE_COMPETITION);
    }
}

