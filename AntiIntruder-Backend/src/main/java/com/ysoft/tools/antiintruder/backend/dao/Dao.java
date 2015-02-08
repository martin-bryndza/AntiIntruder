/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.dao;

/**
 *
 * @author Bato
 * @param <T>
 * @param <U>
 */
public interface Dao<T, U> {

    /*  Create the entity
     * @throws IllegalArgumentException if parameter is null or invalid
     */
    U create(T entity);

    /* Return the entity
     * @throws IllegalArgumentException if parameter is null or invalid
     */
    T get(U pk);

    /* Update the entity
     * @throws IllegalArgumentException if parameter is null, invalid or non-existent in the DB
     */
    void update(T entity);

    /* Remove the entity
     * @throws IllegalArgumentException if parameter is null or invalid. Does not throw this exception if
     * parameter is valid but given entity is nonexistent.
     */
    void remove(U pk);
}
