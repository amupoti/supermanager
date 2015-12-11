package org.amupoti.sm.main.services;

import org.amupoti.sm.main.TestConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

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
    public void testLogin() throws IOException, XPatherException, URISyntaxException {

        restTemplate.getMessageConverters().add( new FormHttpMessageConverter() );

        ResponseEntity<String> exchange = restTemplate.exchange(URL_ENTRY, HttpMethod.GET, null, String.class);
        log.info("Get to "+ URL_ENTRY);
        log.info(exchange.getStatusCode());
        log.info(exchange.getHeaders());
        List<String> cookie = exchange.getHeaders().get("Set-Cookie");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email","Amupoti ");
        params.add("clave","1Eclipse.");
        params.add("entrar", "Entrar");

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add("Cookie", cookie.get(0).split(";")[0]+";__utma=1.1617038714.1442169527.1442182060.1443185071.3; __utmz=1.1443185071.3.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); __utma=1.1617038714.1442169527.1442182060.1443185071.3; __utmz=1.1443185071.3.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); __utmb=1.4.10.1449790169; __utmc=1; __utmt=1");
        httpHeaders.add("Origin", "http://supermanager.acb.com");
        httpHeaders.add("Host","supermanager.acb.com");
        httpHeaders.add("Referer","http://supermanager.acb.com/index/identificar");
        httpHeaders.add(HttpHeaders.ACCEPT,"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpHeaders.add(HttpHeaders.CONTENT_TYPE,"application/x-www-form-urlencoded");
        httpHeaders.add(HttpHeaders.CACHE_CONTROL,"max-age=0");

        httpHeaders.add("Upgrade-Insecure-Requests","1");

        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        exchange = restTemplate.exchange(URL_FORM, HttpMethod.POST, httpEntity, String.class,params);
        log.info("Post to "+URL_FORM+ " with headers "+httpHeaders);
        log.info(exchange.getStatusCode());
        log.info(exchange.getHeaders());
        exchange = restTemplate.exchange(URL_LOGGED_IN, HttpMethod.GET, httpEntity, String.class);
        log.info("Get to "+ URL_LOGGED_IN);
        log.info(exchange.getStatusCode());
        log.info(exchange.getHeaders());
        //Assert.assertTrue(exchange.getBody().contains("crear equipo"));



    }


}

