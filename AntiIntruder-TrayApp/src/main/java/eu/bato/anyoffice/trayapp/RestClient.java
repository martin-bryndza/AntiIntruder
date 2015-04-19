/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import eu.bato.anyoffice.trayapp.entities.InteractionPerson;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
    private static final String PATH = "/api/v1/";
    private static String uri = Configuration.getInstance().getProperty(Property.SERVER_ADDRESS) + PATH;

    private final RestTemplate rest;
    private final HttpHeaders headers;

    private static boolean serverOnline = true;

    public RestClient(Credentials credentials) {
        this.headers = createHeaders(credentials);
        rest = createRestTemplate();
    }

    public static void setServerAddress(String address) {
        uri = address + PATH;
    }

    private static HttpHeaders createHeaders(Credentials credentials) {
        return new HttpHeaders() {
            {
                String authHeader = "Basic " + credentials.getEncodedAuthenticationString();
                set("Authorization", authHeader);
                setContentType(MediaType.APPLICATION_JSON);
            }
        };
    }

    private static RestTemplate createRestTemplate() {
        RestTemplate rest = new RestTemplate();
        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactoryBasicAuth(new HttpHost("localhost", 80)));
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
            response = createRestTemplate().exchange(uri + "login", HttpMethod.GET, new HttpEntity<>(createHeaders(credentials)), String.class);
            log.debug("Login response:" + response.toString());
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to connect to server {}", uri, e.getMessage());
            return false;
        }
    }

    void ping() {
        ResponseEntity<String> response;
        try {
            log.debug("ping");
            response = exchange(uri + "ping", HttpMethod.PUT, new HttpEntity<>(headers), String.class);
            log.debug("ping response:" + response.getStatusCode().toString() + " body:" + response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Ping server failed.");
        }
    }

    PersonState getState() {
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "state", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET state response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseState(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to get state.");
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
            response = exchange(uri + "state", HttpMethod.PUT, entity, String.class);
            log.info("Set state \"{}\" response: {}; body: {}", state, response.getStatusCode().toString(), response.getBody());
            return parseState(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to set state.");
            return PersonState.UNKNOWN;
        }
    }

    boolean isStateChangePossible(PersonState toState) {
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "canchange?state=" + toState.name(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("Is state change possible " + toState + " response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseBoolean(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to find out if change to state " + toState + " is possible.");
            return false;
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
            response = exchange(uri + "location", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET location response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseString(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to get location.");
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
            response = exchange(uri + "location", HttpMethod.PUT, entity, String.class);
            log.info("Set location \"{}\" response: {}", location, response.getStatusCode().toString());
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to set location \"{}\".", location);
            return false;
        }
    }

    int getNumberOfRequests() {
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "requests", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET number of requests response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseInteger(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to GET number of requests.");
            return 0;
        }
    }

    List<InteractionPerson> getNewAvailableConsulters() {
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "availableInteractionPersons", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET new available consulters response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseListInteractionPerson(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to GET new available consulters.");
            return new LinkedList<>();
        }
    }

    long getDndStart() {
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "dndStart", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET dndStart response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseLong(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to GET dndStart.");
            return 0;
        }
    }

    long getDndEnd() {
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "dndEnd", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET dndEnd response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseLong(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to GET dndEnd.");
            return 0;
        }
    }

    static boolean isServerOnline() {
        return serverOnline;
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
            response = exchange(uri + "locked", HttpMethod.PUT, entity, String.class);
            log.debug("Sending info about machine " + (lock ? "" : "un") + "lock response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseState(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to send info about machine " + (lock ? "" : "un") + "lock.");
            return PersonState.UNKNOWN;
        }
    }

    private <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) throws RestClientException {
        ResponseEntity<T> response;
        try {
            response = rest.exchange(url, method, requestEntity, responseType);
            serverOnline = true;
        } catch (RestClientException e) {
            serverOnline = false;
            log.error(e.getMessage());
            throw e;
        }
        return response;
    }

    private PersonState parseState(String responseBody) {
        return PersonState.valueOf(parseString(responseBody));
    }

    private Boolean parseBoolean(String responseBody) {
        return Boolean.valueOf(parseString(responseBody));
    }

    private String parseString(String responseBody) {
        return responseBody.replace("\"", "");
    }

    private Integer parseInteger(String responseBody) {
        return Integer.valueOf(parseString(responseBody));
    }

    private Long parseLong(String responseBody) {
        return Long.valueOf(parseString(responseBody));
    }

    private Map<String, String> parseMapStringString(String responseBody) {
        responseBody = responseBody.replaceAll("[{}]", "");
        String[] items = responseBody.split(",");
        Map<String, String> result = new HashMap<>();
        for (String item : items) {
            String[] parts = item.split(":", 2);
            result.put(parseString(parts[0]), parseString(parts[1]));
        }
        return result;
    }

    private List<String> parseList(String responseBody) {
        List<String> result = new LinkedList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser.parse(responseBody);
            array.stream().forEach((o) -> {
                result.add((String) o);
            });
        } catch (ParseException ex) {
            log.error("Error while parsing List from body: {}", responseBody, ex);
        }
        return result;
    }

    private List<InteractionPerson> parseListInteractionPerson(String responseBody) {
        List<InteractionPerson> result = new LinkedList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser.parse(responseBody);
            array.stream().forEach((o) -> {
                result.add(jsonObjectToInteractionPerson((JSONObject) o));
            });
        } catch (ParseException ex) {
            log.error("Error while parsing List from body: {}", responseBody, ex);
        }
        return result;
    }

    private InteractionPerson parseInteractionPerson(String responseBody) {
        InteractionPerson result = null;
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(responseBody);
            result = jsonObjectToInteractionPerson(obj);
        } catch (ParseException pe) {
            log.error("Error while parsing InteractionPerson from body: {}", responseBody, pe);
        }
        return result;
    }

    private InteractionPerson jsonObjectToInteractionPerson(JSONObject obj) {
        InteractionPerson person = new InteractionPerson();
        person.setId((Long) obj.get("id"));
        person.setDisplayName((String) obj.getOrDefault("displayName", "UNKNOWN"));
        person.setDndStart((Long) obj.getOrDefault("dndStart", null));
        person.setLocation((String) obj.getOrDefault("location", "UNKNOWN"));
        person.setState(PersonState.valueOf((String) obj.getOrDefault("state", "UNKNOWN")));
        person.setUsername((String) obj.get("username"));
        return person;
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
