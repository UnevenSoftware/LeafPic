package org.horaapps.leafpic.delete;

import java.io.File;

public class DeleteException extends Exception {

    private String cause;
    private File file;

    public DeleteException(File file, String cause) {
        this.file = file;
        this.cause = cause;
    }

    @Override
    public String toString() {
        return String.format("%s %s", file.getName(), this.cause);
    }

    @Override
    public String getMessage() {
        return toString();
    }

    @Override
    public String getLocalizedMessage() {
        return toString();
    }
}
