package org.amupoti.supermanager.parser.acb;

import org.amupoti.supermanager.parser.acb.config.TestConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Marcel on 25/09/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)

public class LoginTest {

    private static final String URL_FORM = "http://supermanager.acb.com/index/identificar";
    Log log = LogFactory.getLog(LoginTest.class);
    private static final String URL_ENTRY = "http://supermanager.acb.com/";
    private static final String URL_LOGGED_IN = "http://supermanager.acb.com/inicio/index";

    @Autowired
    private RestTemplate restTemplate;


    @Test
    public void testLogin() throws IOException, URISyntaxException {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Host","supermanager.acb.com");

        httpHeaders.add(HttpHeaders.ACCEPT,"*/*");

        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      //          httpHeaders.add(HttpHeaders.CONTENT_LENGTH, "43");
      //  httpHeaders.add(HttpHeaders.CACHE_CONTROL,"no-cache");
     //   httpHeaders.add(HttpHeaders.USER_AGENT,"User-Agent: curl/7.40.0");

      //  httpHeaders.add(HttpHeaders.CONNECTION,"keep-alive");
     //   httpHeaders.add("Cookie", " __utma=1.1617038714.1442169527.1442182060.1443185071.3; __utmz=1.1443185071.3.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); __utma=1.1617038714.1442169527.1442182060.1443185071.3; __utmz=1.1443185071.3.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); PHPSESSID=farq6p6pdlvmfbr2t1em6tfkh0; __utmb=1.12.10.1449877595; __utmc=1; __utmt=1");
        httpHeaders.add(HttpHeaders.REFERER,"http://supermanager.acb.com/index/identificar");


        restTemplate.getMessageConverters().add(new FormHttpMessageConverter() );


        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email","testsm_testsm");
        params.add("clave","testsm_testsm");
        params.add("entrar", "Entrar");





        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(params, httpHeaders);

        ResponseEntity<String> exchange = restTemplate.postForEntity(URL_FORM, httpEntity, String.class,params);
        log.info("Post to "+URL_FORM+ " with headers "+httpHeaders);
        log.info(exchange.getStatusCode());
        log.info(exchange.getHeaders());
        httpHeaders.add("Cookie", exchange.getHeaders().get("Set-Cookie").toString().replace("[","").split(";")[0]);

        httpEntity = new HttpEntity<MultiValueMap<String, String>>(params, httpHeaders);
        exchange = restTemplate.exchange(URL_LOGGED_IN, HttpMethod.GET, httpEntity, String.class);
        log.info("Get to "+ URL_LOGGED_IN);
        log.info(exchange.getStatusCode());
        log.info(exchange.getHeaders());
        Assert.assertTrue(exchange.getBody().contains("crear equipo"));



    }


}

