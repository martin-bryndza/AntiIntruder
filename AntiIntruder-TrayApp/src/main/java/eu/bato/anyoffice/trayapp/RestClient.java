/* 
 * Copyright (c) 2015, Martin Bryndza
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.entities.PersonState;
import eu.bato.anyoffice.trayapp.entities.Credentials;
import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import eu.bato.anyoffice.trayapp.entities.Consultation;
import eu.bato.anyoffice.trayapp.entities.InteractionPerson;
import eu.bato.anyoffice.trayapp.entities.PendingConsultationState;
import java.io.IOException;
import java.nio.charset.Charset;
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

    private static HttpHeaders createHeaders(final Credentials credentials) {
        return new HttpHeaders() {
            {
                String authHeader = "Basic " + credentials.getEncodedAuthenticationString();
                set("Authorization", authHeader);
                //setContentType(MediaType.APPLICATION_JSON);
                setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
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

    /**
     * Sends a request to login to server.
     *
     * @param credentials
     * @return true, if the authentication succeeded, false otherwise
     */
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

    /**
     * Tries to send ping message to server. Does nothing, if an error occurs.
     */
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

    /**
     * Gets the current state set on server.
     *
     * @return PersonState returned by server or PersonState.UNKNOWN if an error
     * occured
     */
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

    /**
     * Sends a request to change state to server. The returned state may be
     * different, because the server may reject the change.
     *
     * @param state PersonState to set
     * @return the PersonState returned by server
     */
    PersonState setState(PersonState state) {
        HttpEntity<String> entity = createEntity(state);
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

    /**
     * Sends a request to change state to server. The returned state may be
     * different, because the server may reject the change.
     *
     * @param state PersonState to set
     * @return the PersonState returned by server
     */
    PersonState setDndState(Long period) {
        HttpEntity<String> entity = createEntity(period);
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "statednd", HttpMethod.PUT, entity, String.class);
            log.info("Set DND state for period \"{}\" response: {}; body: {}", period, response.getStatusCode().toString(), response.getBody());
            return parseState(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to set dnd state.");
            return PersonState.UNKNOWN;
        }
    }

    /**
     * Sends request to server to prolong DND period.
     *
     * @param millisToAdd milliseconds to add
     * @return new end of DND period in milliseconds or null if an error
     * occurred
     */
    Long addDndTime(Long millisToAdd) {
        HttpEntity<String> entity = createEntity(millisToAdd);
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "adddnd", HttpMethod.PUT, entity, String.class);
            log.info("Add DND time {} millis response: {}; body: {}", millisToAdd, response.getStatusCode().toString(), response.getBody());
            return parseLong(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to add dnd time {} millis.", millisToAdd);
            return null;
        }
    }

    /**
     *
     * @param toState
     * @return possibility to switch to state or false, if an error occurred
     */
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

    /**
     * Sends info to server info about machine unlock.
     *
     * @return Current state of consultation after machine unlock or
 PersonState.UNKNOWN, if an error occurred
     */
    PersonState returnFromAway() {
        return lock(false, false);
    }

    PersonState returnFromAway(boolean throwOnFailure) throws RestClientException {
        return lock(false, throwOnFailure);
    }

    /**
     * Sends info to server info about machine lock.
     *
     * @return Current state of consultation after machine unlock or
 PersonState.UNKNOWN, if an error occurred
     */
    PersonState goAway() {
        return lock(true, false);
    }

    /**
     * Gets location set on server.
     *
     * @return location returned by server or an empty string, if an error
     * occurred
     */
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
     * Sends a request to server to set location.
     *
     * @param location
     * @return true, if location was set successfully, false otherwise
     */
    boolean setLocation(String location) {
        HttpEntity<String> entity = createEntity(location);
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

    /**
     *
     * @return number of newly requested consultations or zero, if an error
     * occurred
     */
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

    /**
     *
     * @return number of people newly available for consultation
     */
    List<Consultation> getActiveIncomingConsultations() {
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "incomingConsultations", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET current incoming consultations response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseListConsultation(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to GET current incoming consultations.");
            return new LinkedList<>();
        }
    }
    
    List<Consultation> getActiveOutgoingConsultations() {
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "outgoingConsultations", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET current outgoing consultations response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseListConsultation(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to GET current outgoing consultations.");
            return new LinkedList<>();
        }
    }

    /**
     *
     * @return time when next DND period will be available or 0, if an error
     * occurred
     */
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

    /**
     *
     * @return time, when the current DND period will end or the last DND period
     * has ended, or 0, if an error occurred
     */
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

    /**
     *
     * @return maximum time possible for a DND period or 0, if an error occurred
     */
    long getDndMax() {
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "dndmax", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET dndmax response:" + response.getStatusCode().toString() + " body:" + response.getBody());
            return parseLong(response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to GET dndmax.");
            return 0;
        }
    }

    /**
     * Sends request to server to note disturbance of the user
     *
     * @param aoUser true, if the disturber uses AnyOffice, false if not and
     * null if the information is unavailable
     */
    void noteDisturbance(Boolean aoUser) {
        HttpEntity<String> entity = createEntity(aoUser);
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "disturbance", HttpMethod.PUT, entity, String.class);
            log.info("Note disturbance of aoUser \"{}\" response: {}; body: {}", aoUser, response.getStatusCode().toString(), response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to note disturbance.");
        }
    }
    
    /**
     * Sends request to server to settle consultation
     *
     * @param consultationId 
     */
    void settleConsultation(Long consultationId) {
        HttpEntity<String> entity = createEntity(consultationId);
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "settleConsultation", HttpMethod.PUT, entity, String.class);
            log.info("Settle consultation with ID \"{}\" response: {}; body: {}", consultationId, response.getStatusCode().toString(), response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to settle consultation.");
        }
    }
    
    void callRequester(Long consultationId) {
        HttpEntity<String> entity = createEntity(consultationId);
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "callRequester", HttpMethod.PUT, entity, String.class);
            log.info("Call requester of consultation with ID \"{}\" response: {}; body: {}", consultationId, response.getStatusCode().toString(), response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to call requester.");
        }
    }
    
    void cancelCallToRequester(Long consultationId) {
        HttpEntity<String> entity = createEntity(consultationId);
        ResponseEntity<String> response;
        try {
            response = exchange(uri + "cancelCallToRequester", HttpMethod.PUT, entity, String.class);
            log.info("Cancel call to requester of consultation with ID \"{}\" response: {}; body: {}", consultationId, response.getStatusCode().toString(), response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to cancel call to requester.");
        }
    }
    
    /**
     *
     * @return true, if the last server request finished successfully, false
     * otherwise
     */
    static boolean isServerOnline() {
        return serverOnline;
    }

    private HttpEntity<String> createEntity(Object bodyObject) {
        HttpEntity<String> entity = null;
        try {
            entity = new HttpEntity<>(new ObjectMapper().writeValueAsString(bodyObject), headers);
        } catch (IOException e) {
            log.error(e.getMessage()); //TODO
        }
        return entity;
    }

    /**
     * Sends info to server info about machine lock/unlock.
     *
     * @param lock
     * @return
     * @throws RestClientException
     */
    private PersonState lock(boolean lock, boolean throwOnFailure) throws RestClientException {
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
            if (throwOnFailure) {
                throw e;
            } else {
                return PersonState.UNKNOWN;
            }
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
            for (Object o : array) {
                result.add((String) o);
            }
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
            for (Object o : array) {
                result.add(jsonObjectToInteractionPerson((JSONObject) o));
            }
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
    
    private List<Consultation> parseListConsultation(String responseBody) {
        List<Consultation> result = new LinkedList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser.parse(responseBody);
            for (Object o : array) {
                result.add(jsonObjectToConsultation((JSONObject) o));
            }
        } catch (ParseException ex) {
            log.error("Error while parsing List of Consultation from body: {}", responseBody, ex);
        }
        return result;
    }

    private Consultation jsonObjectToConsultation(JSONObject obj) {
        Consultation consultation = new Consultation();
        consultation.setId((Long) obj.get("id"));
        consultation.setMessage((String) obj.getOrDefault("message", ""));
        consultation.setTime((Long) obj.getOrDefault("dndStart", null));
        consultation.setPendingState(PendingConsultationState.valueOf((String) obj.get("state")));
        JSONObject requester = (JSONObject) obj.get("requester");
        consultation.setRequesterLocation((String) requester.getOrDefault("location", "UNKNOWN"));
        consultation.setRequesterName((String) requester.getOrDefault("displayName", "UNKNOWN"));
        consultation.setRequesterState(PersonState.valueOf((String) requester.getOrDefault("state", "UNKNOWN")));
        return consultation;
    }

}
