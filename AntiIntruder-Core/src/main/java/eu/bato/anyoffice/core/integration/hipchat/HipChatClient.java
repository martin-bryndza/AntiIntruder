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
package eu.bato.anyoffice.core.integration.hipchat;

import eu.bato.anyoffice.serviceapi.dto.PersonState;
import java.nio.charset.Charset;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author bryndza
 */
@Service
public class HipChatClient {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(HipChatClient.class);
    private static final String PATH = "http://api.hipchat.com/v2/";

    private final RestTemplate rest;
    private final HttpHeaders headers;

    public HipChatClient() {
        this.headers = createHeaders();
        rest = createRestTemplate();
    }

    private static HttpHeaders createHeaders() {
        return new HttpHeaders() {
            {
                setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
            }
        };
    }

    private static RestTemplate createRestTemplate() {
        RestTemplate rest = new RestTemplate();
        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return rest;
    }

    public String getPerson(String token, String email) {
        ResponseEntity<String> response;
        try {
            response = exchange(PATH + "user/" + email + "?auth_token=" + token, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.debug("GET HipChat user {}: " + response.getStatusCode().toString() + " body: " + response.getBody(), email);
            return response.getBody();
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to get HipChat user {}.", email);
            return null;
        }
    }

//    {
//"created": "2014-02-14T10:19:26+00:00",
//"email": "Martin.Bryndza@ysoft.com",
//"group":{
//    "id": 75874,
//    "links": {
//        "self": "https://api.hipchat.com/v2/group/75874"
//        },
//    "name": "Y Soft Corporation"
//    },
//"id": 672803,
//"is_deleted": false,
//"is_group_admin": false,
//"is_guest": false,
//"last_active": "2015-04-23T21:49:13+0000",
//"links": {
//    "self": "https://api.hipchat.com/v2/user/672803"
//    },
//"mention_name": "MartinBryndza",
//"name": "Martin Bryndza",
//"photo_url": "https://s3.amazonaws.com/uploads.hipchat.com/photos/672803/htZA2lwgyyejOfx_125.jpg",
//"presence": {
//    "client": {
//        "type": "http://hipchat.com/client/qt/windows",
//        "version": "2.2.1361"
//        },
//    "is_online": true,
//    "show": "dnd",
//    "status": "diplomkujem"
//    },
//"timezone": "Europe/Berlin",
//"title": "QA Engineer",
//"xmpp_jid": "75874_672803@chat.hipchat.com"
//}
    public void setState(String token, String email, PersonState state, String status) {
        String person = getPerson(token, email);
        if (person == null || person.isEmpty() || state == null) {
            return;
        }
        String replaced;
        if (person.contains("\"show\":")) {
            int start = person.indexOf("\"show\":") + 9; // index of state
            int end = person.indexOf("\"", start); // end index of state
            String currentState = person.substring(start, end);
            log.debug("Person's {} current state: {}", email, currentState);
            replaced = person.replace(currentState, getStateString(state));
            log.debug("Person's {} replaced state: {}", email, replaced);
        } else {
            int start = person.indexOf("\"presence\": {") + 13;
            if (start == -1) {
                log.info("Unable to set state and status for person {} because there is no \"presence\":{ string in the message {}", email, person);
                return;
            } else {
                start += 13;
            }
            replaced = person.substring(0, start) + "\"show\": \"" + getStateString(state) + "\", " + person.substring(start);
        }
        if (status != null && replaced.contains("\"status\":")) {
            int start = replaced.indexOf("\"status\":") + 11; // index of status
            int end = replaced.indexOf("\"", start); // end index of status
            String currentStatus = replaced.substring(start, end);
            log.debug("Person's {} current status: {}", email, currentStatus);
            replaced = replaced.replace(currentStatus, status);
            log.debug("Person's {} replaced status: {}", email, replaced);
        } else if (status != null) {
            int start = replaced.indexOf("\"presence\": {") + 13;
            replaced = replaced.substring(0, start) + "\"status\": \"" + status + "\", " + replaced.substring(start);
        }
        HttpEntity<String> entity = new HttpEntity<>(replaced, headers);
        ResponseEntity<String> response;
        try {
            response = exchange(PATH + "user/" + email + "?auth_token=" + token, HttpMethod.PUT, entity, String.class);
            log.info("Set state and status for person {} \"{}\" response: {}; body: {}", email, state, response.getStatusCode().toString(), response.getBody());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to set state and status for person {}. Outgoing message: {}", email, replaced);
        }
    }

    private String getStateString(PersonState state) {
        switch (state) {
            case AVAILABLE:
                return "chat";
            case AWAY:
                return "away";
            case DO_NOT_DISTURB:
                return "dnd";
            default:
                return "xa";
        }
    }

    private <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) throws RestClientException {
        ResponseEntity<T> response;
        try {
            response = rest.exchange(url, method, requestEntity, responseType);
        } catch (RestClientException e) {
            log.error(e.getMessage());
            throw e;
        }
        return response;
    }

}
