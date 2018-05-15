package org.horaapps.leafpic.progress;

import java.util.ArrayList;

public class ErrorCause {

    private String title;
    private ArrayList<String> causes;

    public ErrorCause(String title, ArrayList<String> causes) {
        this.title = title;
        this.causes = causes;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getCauses() {
        return causes;
    }
}
