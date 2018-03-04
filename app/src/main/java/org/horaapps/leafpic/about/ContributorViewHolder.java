package org.horaapps.leafpic.about;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedViewHolder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Custom view for showing a Developer on About screen.
 */
public class ContributorViewHolder extends ThemedViewHolder {

    @BindView(R.id.developer_profile_image)
    CircleImageView profileImage;
    @BindView(R.id.developer_name)
    TextView devName;
    @BindView(R.id.developer_description)
    TextView devDescription;
    @BindView(R.id.developer_contacts)
    LinearLayout devContacts;

    public ContributorViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, itemView);
    }

    private void setProfileImage(@DrawableRes int profileImage) {
        this.profileImage.setImageResource(profileImage);
    }

    private void setName(@Nullable String name) {
        devName.setText(name);
    }


    private void setDescription(@Nullable String description) {
        devDescription.setText(description);
    }

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {
        super.refreshTheme(themeHelper);
        int borderColor = themeHelper.getInvertedBackgroundColor();
        profileImage.setBorderColor(borderColor);
    }

    public void load(Contributor contributor, ContactListener listener) {
        setName(contributor.getName());
        setDescription(contributor.getDescription());
        setProfileImage(contributor.getProfileImage());

        devContacts.removeAllViews();

        if (contributor.getEmail() != null) {
            ContactButton email = new ContactButton(itemView.getContext());
            email.setText(itemView.getContext().getString(R.string.send_email));
            email.bold();
            email.setOnClickListener(v -> listener.onMailClicked(contributor.getEmail()));
            devContacts.addView(email);
        }

        ArrayList<Contact> contacts = contributor.getContacts();
        for (Contact contact : contacts) {
            ContactButton c = new ContactButton(itemView.getContext());
            c.setText(contact.getLabel());
            c.setOnClickListener(v -> listener.onContactClicked(contact));
            devContacts.addView(c);
        }
    }
}
