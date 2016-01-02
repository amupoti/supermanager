package org.amupoti.supermanager.parser.acb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Created by Marcel on 05/08/2015.
 */
public class HtmlACBProviderService {



    @Autowired
    private RestTemplate restTemplate;



    public String get(String getUrl) throws IOException {

        return restTemplate.getForObject(getUrl, String.class);

    }
}
