package eu.bato.anyoffice.backend.service.common;

import org.springframework.dao.RecoverableDataAccessException;

/**
 * Exception template for wrapping try-catch block around any set of persistence
 * operations, throwing DataAccessExceptions when Exception in the wrapped code
 * itself is thrown. All tested code has to be put into implementation of the
 * abstract doMethod(). This class is used when code is expected to include a
 * non-void return statement.
 */
public abstract class DataAccessExceptionNonVoidTemplate<T, U, V> {

    private final U u;
    private final V v;

    public DataAccessExceptionNonVoidTemplate(U u) {
        this.u = u;
        this.v = null;
    }

    public DataAccessExceptionNonVoidTemplate(U u, V v) {
        this.u = u;
        this.v = v;
    }

    public T tryMethod() {
        T returnedObject;
        try {
            returnedObject = doMethod();
        } catch (Exception ex) {
            throw new RecoverableDataAccessException("Operation failed. " + ex.getMessage(), ex);
        }
        return returnedObject;
    }

    public abstract T doMethod();

    public U getU() {
        return u;
    }

    public V getV() {
        return v;
    }

}
