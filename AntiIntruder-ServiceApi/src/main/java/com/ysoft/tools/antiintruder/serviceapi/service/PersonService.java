package com.ysoft.tools.antiintruder.serviceapi.service;

import com.ysoft.tools.antiintruder.serviceapi.dto.PersonDto;

/**
 *
 * @author Bato
 */
public interface PersonService extends Service<PersonDto>{
    
    /**
     * Verify if Person with given username and password exists.
     *
     * @param username
     * @param password
     * @return Person if person with given username and password exist, null otherwise
     */
    PersonDto login(String username, String password);

    /**
     * Set a new password to person.
     *
     * @param username
     * @param password
     */
    void setPassword(String username, String password);

    /**
     * Create new Person.
     *
     * @param person
     * @param password
     * @return Person's id if registration was successful, null otherwise
     */
    Long register(PersonDto person, String password);
    
}
