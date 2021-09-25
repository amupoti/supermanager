package org.amupoti.supermanager.parser.rdm.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


/**
 * Created by Marcel on 25/09/2015.
 */
@Configuration
@ComponentScan("org.amupoti.supermanager.parser.rdm")
public class RdmConfiguration {

    @Bean
    public RestTemplate getRestTemplate() {
        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}
