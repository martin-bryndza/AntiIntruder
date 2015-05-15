package eu.bato.anyoffice.serviceapi.service;

import java.util.List;

/**
 *
 * @author Bato
 * @param <T>
 */
public interface Service<T> {

    public Long save(T dto);

    public T findOne(Long id);

    public void delete(Long id);

    public List<T> findAll();

}
