package org.horaapps.leafpic.delete;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.MediaHelper;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.liz.ThemeHelper;

import java.util.ArrayList;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dnld on 11/03/18.
 */

@Deprecated
public class DeleteMediaBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "DeleteMediaBottomSheet";
    private static final String EXTRA_MEDIA = "media";

    @BindView(R.id.delete_header) ViewGroup headerLayout;
    @BindView(R.id.delete_progress_bar) DonutProgress progressBar;
    @BindView(R.id.delete_errors) TextView txtErrors;
    @BindView(R.id.delete_done_cancel_sheet)
    AppCompatButton btnDoneCancel;

    private DeleteMediaListener listener;
    private Disposable disposable;
    private boolean done = false;

    @NonNull
    @Deprecated
    public static DeleteMediaBottomSheet make(@Nullable ArrayList<Media> media, @Nullable DeleteMediaListener listener) {
        DeleteMediaBottomSheet deleteMediaBottomSheet = new DeleteMediaBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_MEDIA, media);
        deleteMediaBottomSheet.setArguments(bundle);
        deleteMediaBottomSheet.setCancelable(false);
        deleteMediaBottomSheet.setListener(listener);
        return deleteMediaBottomSheet;
    }

    /**
     * Set the listener for media deletion.
     */
    public void setListener(@Nullable DeleteMediaListener listener) {
        this.listener = listener;
    }

    private void setProgress(int progressPercent) {
        progressBar.setProgress(progressPercent);
        progressBar.setText(getString(R.string.toolbar_selection_count, progressPercent, progressBar.getMax()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_delete_media, container, false);
    }

    private void showErrors(HashSet<String> errors) {
        StringBuilder b = new StringBuilder();
        b.append("<b>").append("Errors:").append("</b>").append("<br/>");

        for (String error : errors)
            b.append("<i>").append(" - ").append(error).append("</i>").append("<br/>");

        txtErrors.setText(StringUtils.html(b.toString()));
        txtErrors.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @OnClick(R.id.delete_done_cancel_sheet)
    void cancelDelete() {
        if (done) {
            dismiss();
        } else {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            Log.wtf(TAG, "delete stop");
            listener.onCompleted();
            dismiss();
        }
    }

    @Override
    public void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
    }

    private void done() {
        done = true;
        btnDoneCancel.setText(R.string.done);
        setCancelable(true);
        listener.onCompleted();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setupViews(view);

        Bundle arguments = getArguments();
        ArrayList<Media> media;
        if (arguments == null || (media = arguments.getParcelableArrayList(EXTRA_MEDIA)) == null) {
            done();
            return;
        }

        progressBar.setMax(media.size());
        setProgress(0);

        HashSet<String> errors = new HashSet<>(0);

        disposable = MediaHelper.deleteMedia(getContext(), media)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pair -> {
                            if (pair.second)
                                listener.onDeleted(pair.first);
                            else
                                errors.add(pair.first.getName());

                            setProgress((int) (progressBar.getProgress() + 1));
                        }, err -> {
                        },
                        () -> {
                            if (errors.size() > 0)
                                showErrors(errors);
                            done();
                        });
    }

    private void setupViews(@NonNull View view) {
        ThemeHelper th = ThemeHelper.getInstanceLoaded(getContext());
        view.setBackgroundColor(th.getBackgroundColor());
        headerLayout.setBackgroundColor(th.getPrimaryColor());
        txtErrors.setTextColor(th.getTextColor());
        progressBar.setFinishedStrokeColor(th.getAccentColor());
        progressBar.setTextColor(th.getTextColor());
    }

    /**
     * Interface for listeners to get callbacks when Media items are deleted.
     */
    public interface DeleteMediaListener {
        void onCompleted();

        void onDeleted(Media media);
    }
}
