package com.pindrop.signup.exceptions;

public class SignupServiceSystemException extends Exception {
    public SignupServiceSystemException(String msg) { super(msg); }
    public SignupServiceSystemException(String msg, Throwable ex) { super(msg, ex); }
}
