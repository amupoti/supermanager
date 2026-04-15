package org.amupoti.supermanager.parser.acb.config;

import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.amupoti.supermanager.parser.acb.teams.SMUserTeamService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
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
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        // Provide defaults for @Value fields used by SmContentProvider in tests
        Properties props = new Properties();
        props.setProperty("privateLeague.id", "http://supermanager.acb.com/privadas/ver/id/72834/tipo/");
        configurer.setProperties(props);
        // Fall back to @Value default values for properties not listed here
        configurer.setIgnoreUnresolvablePlaceholders(false);
        return configurer;
    }

    @Bean
    public RestTemplate getRestTemplate() {
        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .disableRedirectHandling()
                .disableCookieManagement()
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

    @Bean
    public SMUserTeamService getAcbTeamsService() {
        // 2-arg constructor creates its own internal thread pool — fine for tests
        return new SMUserTeamService(getSmContentProvider(), getSmContentParser());
    }

    @Bean
    public SmContentProvider getSmContentProvider() {
        return new SmContentProvider();
    }

    @Bean
    public SmContentParser getSmContentParser() {
        return new SmContentParser();
    }
}
