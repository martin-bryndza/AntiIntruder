/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.service.impl.test;

import com.ysoft.tools.antiintruder.backend.dao.EntityDao;
import com.ysoft.tools.antiintruder.backend.dto.convert.impl.EntityConvert;
import com.ysoft.tools.antiintruder.backend.model.Entity;
import com.ysoft.tools.antiintruder.backend.service.impl.EntityServiceImpl;
import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import com.ysoft.tools.antiintruder.serviceapi.service.EntityService;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Bato
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityServiceTest {

    @InjectMocks
    private final EntityService entityService = new EntityServiceImpl();
    @Mock
    private EntityDao entityDaoMock;
    @Mock
    private EntityConvert entityConvertMock;
    private Entity entity;
    private EntityDto entityDto;
    
    private final Long ENTITY_ID = 42L;

    @Before
    public void setUp() {
        Long time = new java.util.Date().getTime();

        entity = new Entity();
        entity.setUsername("aa");
        entity.setDisplayName("aaa");

        entityDto = new EntityDto();
    }

    @After
    public void tearDown() {
        entity = null;
    }

    @Test
    public void testCreate() {
        entityDto.setId(null);    // must be null if new entity is to be created
        entity.setId(null);     // returned after conversion from DTO
        
        Entity created = new Entity();
        created.setId(ENTITY_ID);
        created.setUsername(entity.getUsername());
        created.setDisplayName(entity.getDisplayName());

        when(entityDaoMock.save(entity)).thenReturn(created);
        when(entityConvertMock.fromDtoToEntity(entityDto)).thenReturn(entity);

        Long returnedId = entityService.save(entityDto);

        ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
        verify(entityDaoMock).save(argument.capture());
        assertTrue("Service layer sent to DAO an entity with different Id than expected. Expected Id: null, "
                + "sent Id: " + argument.getValue().getId() + ".", argument.getValue().getId() == null);

        entity.setId(ENTITY_ID);   // set by DB after DAO create() call
        assertEquals("Returned Id and expected Id are inconsistent.", entity.getId(), returnedId);
    }

}
