package org.amupoti.sm.main.services;

import org.amupoti.sm.main.TestConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
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
    public void testLogin() throws IOException, XPatherException, URISyntaxException {

        HttpHeaders httpHeaders = new HttpHeaders();
       // httpHeaders.add("Origin", "http://supermanager.acb.com");
        httpHeaders.add("Host","supermanager.acb.com");

        httpHeaders.add(HttpHeaders.ACCEPT,"*/*");
       // httpHeaders.add(HttpHeaders.ACCEPT_LANGUAGE,"ca,en-US;q=0.7,en;q=0.3");
      //  httpHeaders.add(HttpHeaders.ACCEPT_ENCODING,"deflate, gzip");

        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                httpHeaders.add(HttpHeaders.CONTENT_LENGTH, "43");
        httpHeaders.add(HttpHeaders.CACHE_CONTROL,"no-cache");
        httpHeaders.add(HttpHeaders.USER_AGENT,"User-Agent: curl/7.40.0");
        //httpHeaders.add("Upgrade-Insecure-Requests","1");
        //httpHeaders.add("X-FirePHP-Version","0.0.6");
        httpHeaders.add(HttpHeaders.CONNECTION,"keep-alive");
        httpHeaders.add("Cookie", " __utma=1.1617038714.1442169527.1442182060.1443185071.3; __utmz=1.1443185071.3.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); __utma=1.1617038714.1442169527.1442182060.1443185071.3; __utmz=1.1443185071.3.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); PHPSESSID=farq6p6pdlvmfbr2t1em6tfkh0; __utmb=1.12.10.1449877595; __utmc=1; __utmt=1");
        httpHeaders.add(HttpHeaders.REFERER,"http://supermanager.acb.com/index/identificar");

/*
Host: supermanager.acb.com
        Accept-Language: ca,en-US;q=0.7,en;q=0.3
        Accept-Encoding: gzip, deflate
        Referer: http://supermanager.acb.com/index/identificar
        Cookie: __utma=1.1617038714.1442169527.1442182060.1443185071.3; __utmz=1.1443185071.3.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); __utma=1.1617038714.1442169527.1442182060.1443185071.3; __utmz=1.1443185071.3.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); PHPSESSID=farq6p6pdlvmfbr2t1em6tfkh0; __utmb=1.12.10.1449877595; __utmc=1; __utmt=1
        Connection: keep-alive

 */
       // HttpEntity<String> httpEntityGet = new HttpEntity<>(httpHeaders);

        restTemplate.getMessageConverters().add(new FormHttpMessageConverter() );

       // ResponseEntity<String> exchange = restTemplate.exchange(URL_ENTRY, HttpMethod.GET, httpEntityGet, String.class);
//        log.info("Get to "+ URL_ENTRY);
  //      log.info(exchange.getStatusCode());
    //    log.info(exchange.getHeaders());
  //      List<String> cookie = exchange.getHeaders().get("Set-Cookie");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email","amupoti");
        params.add("clave","XXXXXX.");
        params.add("entrar", "Entrar");




        //HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(params, httpHeaders);
        //ResponseEntity<String> exchange = restTemplate.exchange(URL_FORM, HttpMethod.POST, httpEntity, String.class);
        ResponseEntity<String> exchange = restTemplate.postForEntity(URL_FORM, httpEntity, String.class,params);
        log.info("Post to "+URL_FORM+ " with headers "+httpHeaders);
        log.info(exchange.getStatusCode());
        log.info(exchange.getHeaders());
        httpEntity = new HttpEntity<MultiValueMap<String, String>>(params, httpHeaders);
        exchange = restTemplate.exchange(URL_LOGGED_IN, HttpMethod.GET, httpEntity, String.class);
        log.info("Get to "+ URL_LOGGED_IN);
        log.info(exchange.getStatusCode());
        log.info(exchange.getHeaders());
       // Assert.assertTrue(exchange.getBody().contains("crear equipo"));



    }


}

