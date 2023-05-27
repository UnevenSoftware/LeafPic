package org.horaapps.leafpic.progress;

public class ProgressException extends Exception {

    private ErrorCause error;

    public ProgressException(ErrorCause error) {
        this.error = error;
    }

    public ProgressException(String error) {
        this.error = new ErrorCause(error);
    }

    public ErrorCause getError() {
        return error;
    }

    @Override
    public String toString() {
        return error.toString();
    }

    @Override
    public String getMessage() {
        return toStringOverride();
    }

    @Override
    public String getLocalizedMessage() {
        return toStringOverride();
    }

    public String toStringOverride() {
        return toString();
    }
}
