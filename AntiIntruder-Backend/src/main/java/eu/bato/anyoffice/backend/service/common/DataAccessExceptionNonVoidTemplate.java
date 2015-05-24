/* 
 * Copyright (c) 2015, Martin Bryndza
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
