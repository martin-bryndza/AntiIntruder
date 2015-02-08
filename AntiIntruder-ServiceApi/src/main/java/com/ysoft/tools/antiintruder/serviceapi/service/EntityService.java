/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.serviceapi.service;

import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Bato
 */
public interface EntityService extends Service<EntityDto>{
    
    public Page<EntityDto> findAll(Pageable pageable);
    
}
