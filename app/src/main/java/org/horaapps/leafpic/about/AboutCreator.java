package org.horaapps.leafpic.about;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.Themed;
import org.horaapps.liz.ViewUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Custom view for showing a Developer on About screen.
 */
public class AboutCreator extends RelativeLayout implements Themed {

    @BindView(R.id.developer_header_image) ImageView headerImage;
    @BindView(R.id.developer_profile_image) CircleImageView profileImage;
    @BindView(R.id.developer_name) TextView devName;
    @BindView(R.id.developer_role) TextView devRole;
    @BindView(R.id.developer_description) TextView devDescription;
    /* @BindView(R.id.developer_mail_link) TextView linkMail;
     @BindView(R.id.developer_google_plus_link) TextView linkGooglePlus;
     @BindView(R.id.developer_github_link) TextView linkGitHub;*/
    @BindView(R.id.developer_contacts)
    LinearLayout devContacts;


    public AboutCreator(@NonNull Context context) {
        this(context, null);
    }

    public AboutCreator(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public AboutCreator(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AboutCreator(@NonNull Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        setupView(context);
        LayoutInflater.from(context).inflate(R.layout.view_about_creator, this, true);
        ButterKnife.bind(this);

        if (attributeSet == null) throw new RuntimeException("Must provide developer details!");
        setupData(context, attributeSet);

        alignProfileImageWithHeader();
    }

    /**
     * Set the listener to be invoked when Profile links are tapped.
     */
    public void setupListeners(ContactListener listener, String mail, ArrayList<Contact> contacts) {

        devContacts.removeAllViews();
        if (mail != null) {
            ContactButton email = new ContactButton(getContext());
            email.setText(getContext().getString(R.string.send_email));
            email.bold();
            email.setOnClickListener(v -> listener.onMailClicked(mail));
            devContacts.addView(email);
        }

        for (Contact contact : contacts) {
            ContactButton c = new ContactButton(getContext());
            c.setText(contact.getLabel());
            c.setOnClickListener(v -> listener.onContactClicked(contact));
            devContacts.addView(c);
        }
    }

    private void alignProfileImageWithHeader() {
        headerImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                headerImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int profileImageSize = profileImage.getMeasuredHeight();
                int headerImageHeight = headerImage.getMeasuredHeight();

                // Set the profile image to align with bottom of header
                LayoutParams layoutParams = (LayoutParams) profileImage.getLayoutParams();
                layoutParams.topMargin = headerImageHeight - (int) (0.5f * profileImageSize);
                profileImage.setLayoutParams(layoutParams);
            }
        });
    }

    private void setupView(@NonNull Context context) {
        setBackground(ContextCompat.getDrawable(context, R.drawable.ripple));
        int verticalPadding = getResources().getDimensionPixelOffset(R.dimen.developer_small_Spacing);
        setPadding(0, 0, 0, verticalPadding);
    }

    private void setupData(@NonNull Context context, @NonNull AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.AboutCreator);
        Drawable headerImage = typedArray.getDrawable(R.styleable.AboutCreator_headerImage);
        Drawable profileImage = typedArray.getDrawable(R.styleable.AboutCreator_displayImage);
        String displayName = typedArray.getString(R.styleable.AboutCreator_name);
        String roleText = typedArray.getString(R.styleable.AboutCreator_role);
        String descriptionText = typedArray.getString(R.styleable.AboutCreator_description);
        typedArray.recycle();

        setHeaderImage(headerImage);
        setProfileImage(profileImage);
        setName(displayName);
        setRole(roleText);
        setDescription(descriptionText);
    }

    private void setHeaderImage(@Nullable Drawable drawable) {
        headerImage.setImageDrawable(drawable);
    }

    private void setProfileImage(@Nullable Drawable drawable) {
        profileImage.setImageDrawable(drawable);
    }

    private void setName(@Nullable String name) {
        devName.setText(name);
    }

    private void setRole(@Nullable String role) {
        devRole.setText(role);
    }

    private void setDescription(@Nullable String description) {
        devDescription.setText(description);
    }

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {
        int textColor = themeHelper.getTextColor();
        int subTextColor = themeHelper.getSubTextColor();
        int borderColor = themeHelper.getInvertedBackgroundColor();

        profileImage.setBorderColor(borderColor);
        devName.setTextColor(textColor);
        devRole.setTextColor(subTextColor);
        devDescription.setTextColor(subTextColor);

        for (View view : ViewUtil.getAllChildren(devContacts)) {
            if (view instanceof Themed) ((Themed) view).refreshTheme(themeHelper);
        }

    }
}
