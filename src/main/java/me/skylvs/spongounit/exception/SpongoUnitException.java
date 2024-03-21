package me.skylvs.spongounit.exception;

public class SpongoUnitException extends RuntimeException {

    public SpongoUnitException() {
    }

    public SpongoUnitException(String message) {
        super(message);
    }

    public SpongoUnitException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpongoUnitException(Throwable cause) {
        super(cause);
    }

    public SpongoUnitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
