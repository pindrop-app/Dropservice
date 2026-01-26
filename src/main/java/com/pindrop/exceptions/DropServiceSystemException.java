package com.pindrop.exceptions;

/**
 * DropService exceptions which catches unexpected system bahavior exceptions like IO Exceptions and Networking issues.
 */
public class DropServiceSystemException extends Exception {
    public DropServiceSystemException(String msg) {
        super(msg);
    }

    public DropServiceSystemException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
