package org.amupoti.sm.main.config;

import org.amupoti.supermanager.parser.acb.SMUserTeamService;
import org.amupoti.supermanager.parser.acb.SMUserTeamServiceImpl;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.amupoti.supermanager.parser.acb.SmContentProvider.EUROPEO_HOME_URL;

/**
 * Created by Marcel on 06/08/2015.
 */
@Configuration
@PropertySource("classpath:urls.properties")
public class ApplicationConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public RestTemplate getRestTemplate(){
        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .disableRedirectHandling()
                .disableCookieManagement()
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }

    @Bean
    public SMUserTeamService getAcbTeamService() {
        return new SMUserTeamServiceImpl();
    }

    @Bean
    public SmContentProvider getSmContentProvider() {
        return new SmContentProvider(EUROPEO_HOME_URL);
    }
}

