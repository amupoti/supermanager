package org.amupoti.sm.main;

import org.amupoti.sm.main.services.provider.HTMLProviderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Marcel on 25/09/2015.
 */
@PropertySource("classpath:urls.properties")
//@ComponentScan("org.amupoti.sm.main.services.provider")
public class TestConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public HTMLProviderService getHtmlProviderService(){
        return new HTMLProviderService();
    }

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

}
