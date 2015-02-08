/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.dao.impl;

import com.ysoft.tools.antiintruder.backend.dao.StateDao;
import com.ysoft.tools.antiintruder.backend.model.State;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Bato
 */
@Repository
public class StateDaoImpl implements StateDao{

    @Override
    public Long create(State entity) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public State get(Long pk) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(State entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(Long pk) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
