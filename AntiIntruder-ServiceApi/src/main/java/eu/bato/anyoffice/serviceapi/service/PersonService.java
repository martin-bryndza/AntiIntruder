package eu.bato.anyoffice.serviceapi.service;

import eu.bato.anyoffice.serviceapi.dto.LoginDetailsDto;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
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
    
    void updateState(Long id, PersonState personState);
    
    Optional<PersonDto> findOneByUsername(String username);
    
}
