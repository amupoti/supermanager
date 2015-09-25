package org.amupoti.sm.main;
import org.amupoti.sm.main.services.provider.HTMLProviderService;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * Created by Marcel on 25/09/2015.
 */
@PropertySource("classpath:urls.properties")
@ComponentScan("org.amupoti.sm.main.services.provider")
public class TestConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public HTMLProviderService getHtmlProviderService(){
        return new LocalHTMLProviderService();
    }

    private class LocalHTMLProviderService extends HTMLProviderService{

        @Override
        public String get(String getUrl) throws IOException {

            return IOUtils.toString(new ClassPathResource(getUrl).getInputStream());
        }
    }


}
