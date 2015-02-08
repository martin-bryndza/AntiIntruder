/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.serviceapi.dto.convert;

/**
 *
 * @author Bato
 */
public interface Convert<T, U> {

    /*
     * @param dto transfer object U
     * @param em EntityManager passed from the calling object.
     * @return entity T: some of its parameters may be null mostly in case of newly-to-be-created entity 
     */
    public T fromDtoToEntity(U dto);

    /*
     * @param entity T
     * @return transfer object U
     * @throws IllegalArgumentException if entity T has no id.
     */
    public U fromEntityToDto(T entity);
}
