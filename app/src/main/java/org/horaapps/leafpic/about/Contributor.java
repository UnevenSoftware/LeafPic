package org.horaapps.leafpic.about;

import android.support.annotation.DrawableRes;
import java.util.ArrayList;

/**
 * Created by dnld on 04/03/18.
 */
public class Contributor {

    private String name;

    private String description;

    private String email;

    @DrawableRes
    private int profileImage;

    private ArrayList<Contact> contacts;

    public Contributor(String name, String description, @DrawableRes int profileImage) {
        this.name = name;
        this.description = description;
        this.profileImage = profileImage;
        this.contacts = new ArrayList<>();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void addSocial(String label, String url) {
        Contact c = new Contact(url, label);
        contacts.add(c);
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getProfileImage() {
        return profileImage;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }
}
