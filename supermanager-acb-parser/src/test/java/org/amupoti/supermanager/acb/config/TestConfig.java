package org.amupoti.supermanager.acb.config;

import org.amupoti.supermanager.acb.adapter.out.acbapi.AcbAuthenticationAdapter;
import org.amupoti.supermanager.acb.adapter.out.acbapi.AcbMarketDataAdapter;
import org.amupoti.supermanager.acb.adapter.out.acbapi.AcbPlayerChangeAdapter;
import org.amupoti.supermanager.acb.adapter.out.acbapi.AcbPlayerStatsAdapter;
import org.amupoti.supermanager.acb.adapter.out.acbapi.AcbTeamDataAdapter;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@Configuration
public class TestConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        Properties props = new Properties();
        props.setProperty("privateLeague.id", "http://supermanager.acb.com/privadas/ver/id/72834/tipo/");
        configurer.setProperties(props);
        configurer.setIgnoreUnresolvablePlaceholders(false);
        return configurer;
    }

    @Bean
    public RestTemplate restTemplate() {
        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .disableRedirectHandling()
                .disableCookieManagement()
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        return restTemplate;
    }

    @Bean
    public AcbAuthenticationAdapter acbAuthenticationAdapter(RestTemplate restTemplate) {
        return new AcbAuthenticationAdapter(restTemplate);
    }

    @Bean
    public AcbTeamDataAdapter acbTeamDataAdapter(RestTemplate restTemplate) {
        return new AcbTeamDataAdapter(restTemplate);
    }

    @Bean
    public AcbMarketDataAdapter acbMarketDataAdapter(RestTemplate restTemplate) {
        return new AcbMarketDataAdapter(restTemplate);
    }

    @Bean
    public AcbPlayerStatsAdapter acbPlayerStatsAdapter(RestTemplate restTemplate) {
        return new AcbPlayerStatsAdapter(restTemplate);
    }

    @Bean
    public AcbPlayerChangeAdapter acbPlayerChangeAdapter(RestTemplate restTemplate) {
        return new AcbPlayerChangeAdapter(restTemplate);
    }
}
