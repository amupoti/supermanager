package org.amupoti.supermanager.parser.acb.config;

import org.amupoti.supermanager.parser.acb.ACBTeamService;
import org.amupoti.supermanager.parser.acb.ACBTeamServiceDefault;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Marcel on 25/09/2015.
 */
@Configuration
public class TestConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    @Bean
    public ACBTeamService getAcbTeamsService(){
        return new ACBTeamServiceDefault();
    }

}
