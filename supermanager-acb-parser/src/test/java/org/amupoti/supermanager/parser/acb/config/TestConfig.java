package org.amupoti.supermanager.parser.acb.config;

import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.amupoti.supermanager.parser.acb.teams.SMUserTeamService;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;


/**
 * Created by Marcel on 25/09/2015.
 */
@Configuration
public class TestConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        Properties props = new Properties();
        props.setProperty("privateLeague.id", "http://supermanager.acb.com/privadas/ver/id/72834/tipo/");
        propertySourcesPlaceholderConfigurer.setProperties(props);
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public RestTemplate getRestTemplate() {
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
    public SMUserTeamService getAcbTeamsService() {
        return new SMUserTeamService(getSmContentProvider(), getSmContentParser());
    }

    @Bean
    public SmContentProvider getSmContentProvider() {
        return new SmContentProvider(SmContentProvider.ACTIVE_COMPETITION);
    }

    @Bean
    public SmContentParser getSmContentParser() {
        return new SmContentParser();
    }

}
