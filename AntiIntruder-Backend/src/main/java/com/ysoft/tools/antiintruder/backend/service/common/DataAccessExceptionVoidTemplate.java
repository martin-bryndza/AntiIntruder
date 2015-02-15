package com.ysoft.tools.antiintruder.backend.service.common;

import org.springframework.dao.RecoverableDataAccessException;

/**
 * Exception template for wrapping try-catch block around any set of
 * persistence operations, throwing DataAccessExceptions when Exception in the
 * wrapped code itself is thrown. 
 * All tested code has to be put into implementation of the abstract doMethod().
 * This class is used when code is expected to include no return statement.
*/
public abstract class DataAccessExceptionVoidTemplate<U, V> {
        private final U u;
        private final V v;

    public DataAccessExceptionVoidTemplate(U u) {
        this.u = u;
        this.v = null;
    }
    
    public DataAccessExceptionVoidTemplate(U u, V v) {
        this.u = u;
        this.v = v;
    }
    
    public void tryMethod() {
        try {
            doMethod();
        } catch (Exception ex) {
            throw new RecoverableDataAccessException("Operation failed." + ex.getMessage(), ex);
        }
    }
    
    public abstract void doMethod();
    
    public U getU() {
        return u;
    }
       
    public V getV() {
        return v;
    }

}
