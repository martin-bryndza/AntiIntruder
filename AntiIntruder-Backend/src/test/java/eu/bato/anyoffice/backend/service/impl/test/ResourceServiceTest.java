///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package eu.bato.anyoffice.backend.service.impl.test;
//
//import eu.bato.anyoffice.backend.dao.ResourceDao;
//import eu.bato.anyoffice.backend.model.Entity;
//import eu.bato.anyoffice.backend.model.Resource;
//import eu.bato.anyoffice.backend.service.impl.ResourceServiceImpl;
//import eu.bato.anyoffice.serviceapi.dto.EntityDto;
//import eu.bato.anyoffice.serviceapi.dto.ResourceDto;
//import eu.bato.anyoffice.serviceapi.service.ResourceService;
//import org.junit.After;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import org.mockito.runners.MockitoJUnitRunner;
//
///**
// *
// * @author Bato
// */
//@RunWith(MockitoJUnitRunner.class)
////@RunWith(PowerMockRunner.class)
////@PrepareForTest(ResourceConvert.class)
//public class ResourceServiceTest {
//
//    @InjectMocks
//    private final ResourceService entityService = new ResourceServiceImpl();
//    @Mock
//    private ResourceDao entityDaoMock;
//    private Resource entity;
//    private ResourceDto entityDto;
//    
//    private final Long ENTITY_ID = 42L;
//
//    @Before
//    public void setUp() {
//        Long time = new java.util.Date().getTime();
//
//        entity = new Resource();
//        entity.setDescription("aa");
//        entity.setDisplayName("aaa");
//
//        entityDto = new ResourceDto();
//    }
//
//    @After
//    public void tearDown() {
//        entity = null;
//    }
//
//    @Test
//    public void testCreate() {
//        entityDto.setId(null);    // must be null if new entity is to be created
//        entity.setId(null);     // returned after conversion from DTO
//        
//        Entity created = new Entity();
//        created.setId(ENTITY_ID);
//        created.setDescription(entity.getDescription());
//        created.setDisplayName(entity.getDisplayName());
//
//        when(entityDaoMock.save(entity)).thenReturn(created);
////        mockStatic(ResourceConvert.class);
////        expect(ResourceConvert.fromDtoToEntity(entityDto)).andReturn(entity);
////        replay(ResourceConvert.class);
//
//        Long returnedId = entityService.save(entityDto);
//
//        ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
//        verify(entityDaoMock).save(argument.capture());
//        assertTrue("Service layer sent to DAO an entity with different Id than expected. Expected Id: null, "
//                + "sent Id: " + argument.getValue().getId() + ".", argument.getValue().getId() == null);
//
//        entity.setId(ENTITY_ID);   // set by DB after DAO create() call
//        assertEquals("Returned Id and expected Id are inconsistent.", entity.getId(), returnedId);
//    }
//
//}
