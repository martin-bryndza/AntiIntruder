/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpHost;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Bato
 */
public class RestClient {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(RestClient.class);
    private final static String URI = Configuration.getInstance().getProperty(Property.REST_SERVER_ADDRESS) + "/api/v1/state";
    private final static HttpEntity<String> EMPTY_JSON_ENTITY;
    static {
        HttpHeaders headers  = new HttpHeaders();
        headers.setContentType (MediaType.APPLICATION_JSON);
        EMPTY_JSON_ENTITY = new HttpEntity<>(headers);
    }

    private final RestTemplate rest;

    public RestClient() {
        rest = new RestTemplate();
        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactoryBasicAuth(new HttpHost("localhost", 8080)));
    }

    PersonState getState() {
        ResponseEntity<String> response;
        try {
            response = rest.exchange(URI, HttpMethod.GET, EMPTY_JSON_ENTITY, String.class);
            log.debug("GET state response:" + response.toString());
            //return response.getBody();
            return PersonState.valueOf(response.getBody().replace("\"", ""));
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to get state.", e);
            return PersonState.UNKNOWN;
        }
    }
    
    void setState(PersonState state) {
        HttpEntity<String> entity = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try{
            String json = new ObjectMapper().writeValueAsString(state);
            System.out.println("JSON: " + json);
            entity = new HttpEntity<>(json, headers);
        } catch (IOException e){
            log.error(e.getMessage());
        }
        System.out.println("Entity: " + entity);
        rest.exchange(URI, HttpMethod.PUT, entity, String.class);
        //rest.put(URI, entity);
    }
  
}
