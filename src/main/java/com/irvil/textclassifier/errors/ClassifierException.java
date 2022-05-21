package com.irvil.textclassifier.errors;

public class ClassifierException extends Exception {

    public ClassifierException() {
        super();
    }

    public ClassifierException(String message) {
        super(message);
    }

    public ClassifierException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassifierException(Throwable cause) {
        super(cause);
    }

    protected ClassifierException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
