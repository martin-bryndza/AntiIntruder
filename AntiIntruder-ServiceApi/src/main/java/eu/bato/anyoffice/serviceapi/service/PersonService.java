package eu.bato.anyoffice.serviceapi.service;

import eu.bato.anyoffice.serviceapi.dto.LoginDetailsDto;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bato
 */
public interface PersonService extends Service<PersonDto> {
    
//    /**
//     * Verify if Person with given username and password exists.
//     *
//     * @param username
//     * @param password
//     * @return Person if person with given username and password exist, null otherwise
//     */
//    PersonDto login(String username, String password);
    
    /**
     * Gets hashed password of the Person with username.
     * @param username
     * @return Hashed password for the username or null, if such password does not exist.
     */
    Optional<LoginDetailsDto> getLoginDetails(String username);

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
    
    String getUsername(Long id);
    
    List<String> findAllUsernames();
    
    /**
     * Sets state of Person with id to personState, if possible. Otherwise throws IllegalArgumentException
     * @param id
     * @param personState 
     */
    void setState(Long id, PersonState personState);
    
    /**
     * Gets the current state of Person with id.
     * @param id
     * @return 
     */
    PersonState getState(Long id);
    
    /**
     * Sets state of Person with username to personState, if possible. Otherwise
     * throws IllegalArgumentException
     *
     * @param username
     * @param personState
     */
    void setState(String username, PersonState personState);

    /**
     * Gets the current state of Person with username.
     *
     * @param username
     * @return
     */
    PersonState getState(String username);
    
    PersonDto findOneByUsername(String username);
    
    /**
     * 
     * @param username
     * @param dndStart the start of DND or empty to keep unchanged
     * @param dndEnd the end of DND or empty to keep unchanged
     * @param awayStart the start of AWAY or empty to delete the value
     */
    void setTimers(String username, Optional<Date> dndStart, Optional<Date> dndEnd, Optional<Date> awayStart);
    
    boolean isPresent(String username);
    
}
