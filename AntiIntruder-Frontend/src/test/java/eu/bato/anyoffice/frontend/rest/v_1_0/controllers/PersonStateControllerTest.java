/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.frontend.rest.v_1_0.controllers;

import eu.bato.anyoffice.frontend.rest.v1_0.controllers.PersonStateController;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Bato
 */
@RunWith(MockitoJUnitRunner.class)
public class PersonStateControllerTest {
    
    @InjectMocks
    private PersonStateController personStateController = new PersonStateController();
    @Mock
    PersonService personServiceMock;
    
    @Before
    public void setUp() {
        
    }
    
    @Test
    public void testSetCurrentDate(){
        personStateController.setCurrentState("DO_NOT_DISTURB");
    }
    
}
