package org.amupoti.sm.main.service.privateleague;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

/**
 * Created by amupoti on 05/10/2019.
 */
public class TestConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        Properties props = new Properties();
        props.setProperty("privateLeague.id", "http://supermanager.acb.com/privadas/ver/id/72834/tipo/");
        propertySourcesPlaceholderConfigurer.setProperties(props);
        return propertySourcesPlaceholderConfigurer;
    }

}
