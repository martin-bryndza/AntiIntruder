/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import org.apache.http.HttpHost;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Bato
 */
public class RestClient {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(RestClient.class);
    private final static String URI = Configuration.getInstance().getProperty(Property.REST_SERVER_ADDRESS) + "/api/v1/";

    private final RestTemplate rest;

    public RestClient() {
        rest = new RestTemplate();
        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactoryBasicAuth(new HttpHost("localhost", 8080)));
    }
    
    boolean isCorrectCredentials(Credentials credentials){
        ResponseEntity<String> response;
        try {
            response = rest.exchange(URI + "login", HttpMethod.GET, new HttpEntity<>(createHeaders(credentials)), String.class);
            log.debug("GET response:" + response.toString());
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException | IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    PersonState getState() {
        ResponseEntity<String> response;
        try {
            response = rest.exchange(URI + "state", HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class);
            log.debug("GET state response:" + response.toString());
            return PersonState.valueOf(response.getBody().replace("\"", ""));
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to get state.", e);
            return PersonState.UNKNOWN;
        }
    }
    
    void setState(PersonState state) {
        HttpEntity<String> entity = null;
        try{
            entity = new HttpEntity<>(new ObjectMapper().writeValueAsString(state), createHeaders());
        } catch (IOException e){
            log.error(e.getMessage()); //TODO
        }
        rest.exchange(URI + "state", HttpMethod.PUT, entity, String.class);
    }
    
    private HttpHeaders createHeaders() {
        Credentials cred = CurrentUser.getInstance().getCredentials();
        return createHeaders(cred);
    }
    
    private HttpHeaders createHeaders(Credentials cred) {
        return new HttpHeaders() {
            {
                String authHeader = "Basic " + cred.getEncodedAuthenticationString();
                set("Authorization", authHeader);
                setContentType(MediaType.APPLICATION_JSON);
            }
        };
    }
  
}
