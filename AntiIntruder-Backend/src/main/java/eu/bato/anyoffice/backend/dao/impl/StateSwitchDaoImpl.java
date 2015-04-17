/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.bato.anyoffice.backend.dao.impl;

import eu.bato.anyoffice.backend.dao.StateSwitchDao;
import eu.bato.anyoffice.backend.model.StateSwitch;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author bryndza
 */
@Repository
@Transactional
public class StateSwitchDaoImpl implements StateSwitchDao{

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public List<StateSwitch> findRangeForUser(Long id, Date from, Date to) {
        TypedQuery<StateSwitch> query = em.createQuery("SELECT tbl FROM StateSwitch tbl WHERE tbl.personId = :id AND tbl.time >= :from AND tbl.time < :to", StateSwitch.class)
                .setParameter("id", id)
                .setParameter("from", from)
                .setParameter("to", to);
        List<StateSwitch> result = query.getResultList();
        return result;
    }   
    
}
