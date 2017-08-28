package org.amupoti.sm.main.config;

import org.amupoti.supermanager.parser.acb.ACBTeamService;
import org.amupoti.supermanager.parser.acb.ACBTeamServiceDefault;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Marcel on 06/08/2015.
 */
@Configuration
@PropertySource("classpath:urls.properties")
public class ApplicationConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("playerData");
    }

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
    public ACBTeamService getAcbTeamService(){
        return new ACBTeamServiceDefault();
    }
}

