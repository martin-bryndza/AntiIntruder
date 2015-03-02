/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Bato
 */
public class RestClient {
    
    final static org.slf4j.Logger log = LoggerFactory.getLogger(RestClient.class);
    
    RestTemplate rest;

    public RestClient() {
        rest = new RestTemplate();

        HttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
        HttpMessageConverter stringHttpMessageConverternew = new StringHttpMessageConverter();
        List<HttpMessageConverter<?>> converters = new LinkedList<>();
        converters.add(formHttpMessageConverter);
        converters.add(stringHttpMessageConverternew);
        rest.setMessageConverters(converters);
    }
    
    PersonState getState(){
        String stateString = rest.getForObject(Configuration.getInstance().getProperty(Property.REST_SERVER_ADDRESS) + "/rest/status", String.class);
        System.out.println(stateString);
        PersonState state;
        try{
            state = PersonState.valueOf(stateString);
        } catch (IllegalArgumentException e){
            state = PersonState.UNKNOWN;
        }
        return state;
    }
    
    void login(){
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", "admin");
        map.add("password", "admin");
        HttpHeaders headForHeaders = rest.headForHeaders(Configuration.getInstance().getProperty(Property.REST_SERVER_ADDRESS) + "/rest/login", map);
        System.out.println("HEADERS: " + headForHeaders.toString());
        String result = rest.postForObject(Configuration.getInstance().getProperty(Property.REST_SERVER_ADDRESS) + "/rest/login", map, String.class);
        System.out.println("RESULT: " + result);
    }
    
}
