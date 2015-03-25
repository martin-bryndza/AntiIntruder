/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.HttpHost;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
    private final static String URI = Configuration.getInstance().getProperty(Property.SERVER_ADDRESS) + "/api/v1/";

    private final RestTemplate rest;
    private final HttpHeaders headers;

    public RestClient(Credentials credentials) {
        this.headers = createHeaders(credentials);
        rest = createRestTemplate();
    }
    
    private static HttpHeaders createHeaders(Credentials credentials){
        return new HttpHeaders() {
            {
                String authHeader = "Basic " + credentials.getEncodedAuthenticationString();
                set("Authorization", authHeader);
                setContentType(MediaType.APPLICATION_JSON);
            }
        };
    }
    
    private static RestTemplate createRestTemplate(){
        RestTemplate rest = new RestTemplate();
        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactoryBasicAuth(new HttpHost("localhost", 8080)));
//        PersonStateMessageConverter converter = new PersonStateMessageConverter();
//        List<MediaType> mediaTypes = new ArrayList<>();
//        mediaTypes.add(MediaType.APPLICATION_JSON);
//        converter.setSupportedMediaTypes(mediaTypes);
//        List<HttpMessageConverter<?>> converters = rest.getMessageConverters();
//        converters.add(converter);
//        rest.setMessageConverters(converters);
        return rest;
    }

    static boolean isCorrectCredentials(Credentials credentials) {
        ResponseEntity<String> response;
        try {
            
            response = createRestTemplate().exchange(URI + "login", HttpMethod.GET, new HttpEntity<>(createHeaders(credentials)), String.class);
            log.debug("Login response:" + response.toString());
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException | IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    PersonState getState() {
        ResponseEntity<String> response;
        try {
            response = rest.exchange(URI + "state", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET state response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseState(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to get state.", e);
            return PersonState.UNKNOWN;
        }
    }

    PersonState setState(PersonState state) {
        HttpEntity<String> entity = null;
        try {
            entity = new HttpEntity<>(new ObjectMapper().writeValueAsString(state), headers);
        } catch (IOException e) {
            log.error(e.getMessage()); //TODO
        }
        ResponseEntity<String> response;
        try {
            response = rest.exchange(URI + "state", HttpMethod.PUT, entity, String.class);
            log.info("Set state response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseState(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to get state.", e);
            return PersonState.UNKNOWN;
        }
    }

    boolean isStateChangePossible(PersonState toState) {
        ResponseEntity<String> response;
        try {
            response = rest.exchange(URI + "canchange?state="+toState.name(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("Is state change possible " + toState + " response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseBoolean(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to find out if change to state " + toState + " is possible.", e);
            return true;
        }
    }

    PersonState returnFromAway() {
        return lock(false);
    }

    PersonState goAway() {
        return lock(true);
    }
    
    String getLocation() {
        ResponseEntity<String> response;
        try {
            response = rest.exchange(URI + "location", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET location response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseString(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to get location.", e);
            return new String();
        }
    }

    /**
     * 
     * @param location
     * @return true, if location was set successfully, false otherwise
     */
    boolean setLocation(String location) {
        HttpEntity<String> entity = null;
        try {
            entity = new HttpEntity<>(new ObjectMapper().writeValueAsString(location), headers);
        } catch (IOException e) {
            log.error(e.getMessage()); //TODO
        }
        ResponseEntity<String> response;
        try {
            response = rest.exchange(URI + "location", HttpMethod.PUT, entity, String.class);
            log.info("Set location response:" + response.getStatusCode().toString());
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to set location.", e);
            return false;
        }
    }
    
    int getNumberOfRequests(){
        return 5;
    }
    
    List<String> getNewAvailableConsulters(){
        List<String> result = new LinkedList<>();
        result.add("Olda");
        result.add("Peter");
        return result;
    }

    private PersonState lock(boolean lock) {
        HttpEntity<String> entity;
        try {
        entity = new HttpEntity<>(new ObjectMapper().writeValueAsString(lock), headers);
        } catch (IOException e) {
            log.error(e.getMessage()); //TODO
            return PersonState.UNKNOWN;
        }
        ResponseEntity<String> response;
        try {
            response = rest.exchange(URI + "locked", HttpMethod.PUT, entity, String.class);
            log.debug("Sending info about machine " + (lock ? "" : "un") + "lock response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseState(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to send info about machine " + (lock ? "" : "un") + "lock.", e);
            return PersonState.UNKNOWN;
        }
    }
    
    private PersonState parseState(String responseBody){
        return PersonState.valueOf(parseString(responseBody));
    }
    
    private Boolean parseBoolean(String responseBody){
        return Boolean.valueOf(parseString(responseBody));
    }
    
    private String parseString(String responseBody) {
        return responseBody.replace("\"", "");
    }

//    private class PersonStateMessageConverter extends AbstractHttpMessageConverter<PersonState> {
//
//        public PersonStateMessageConverter() {
//        }
//
//        public PersonStateMessageConverter(MediaType supportedMediaType) {
//            super(supportedMediaType);
//        }
//
//        public PersonStateMessageConverter(MediaType... supportedMediaTypes) {
//            super(supportedMediaTypes);
//        }
//
//        @Override
//        protected boolean supports(Class<?> clazz) {
//            return PersonState.class.equals(clazz);
//        }
//
//        @Override
//        protected PersonState readInternal(Class<? extends PersonState> clazz, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
//            java.util.Scanner s = new java.util.Scanner(httpInputMessage.getBody()).useDelimiter("\\A");
//            String theString = s.hasNext() ? s.next() : "";
//            PersonState state;
//            try {
//                state = PersonState.valueOf(theString.replace("\"", ""));
//            } catch (IllegalArgumentException e) {
//                log.error("Unable to parse PersonState from name " + theString + " provided by server.");
//                return PersonState.UNKNOWN;
//            }
//            return state;
//        }
//
//        @Override
//        protected void writeInternal(PersonState state, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
//            OutputStreamWriter out = null;
//            try {
//                out = new OutputStreamWriter(httpOutputMessage.getBody());
//                out.write(state.name());
//            } catch (IOException e) {
//                log.error("Unable to write to HttpOutputMessage.", e);
//            } finally {
//                if (out != null) {
//                    try {
//                        out.close();
//                    } catch (IOException e) {
//                        out = null;
//                    }
//                }
//            }
//        }
//
//    }

}
