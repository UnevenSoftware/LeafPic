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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.MediaHelper;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.liz.ThemeHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dnld on 11/03/18.
 */

public class DeleteMediaBottomSheet extends BottomSheetDialogFragment {

    public interface DeleteMediaListener {
        void onCompleted();

        void onDeleted(Media media);
    }


    public static final String TAG = "delete_bootomsheet";
    public static final String EXTRA_MEDIA = "media";

    @BindView(R.id.header)
    RelativeLayout header;

    @BindView(R.id.cancel_delete)
    AppCompatButton cancelButton;

    @BindView(R.id.delete_progress_bar)
    DonutProgress progress;

    @BindView(R.id.txt_errors)
    TextView txtErrors;

    private ArrayList<Media> media;

    DeleteMediaListener listener;
    boolean cancelRequested = false;

    CompositeDisposable disposable = new CompositeDisposable();

    public void setListener(DeleteMediaListener listener) {
        this.listener = listener;
    }

    public static DeleteMediaBottomSheet make(ArrayList<Media> media, DeleteMediaListener listener) {
        DeleteMediaBottomSheet deleteMediaBottomSheet = new DeleteMediaBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_MEDIA, media);
        deleteMediaBottomSheet.setArguments(bundle);
        deleteMediaBottomSheet.setCancelable(false);
        deleteMediaBottomSheet.setListener(listener);
        return deleteMediaBottomSheet;
    }

    private void setProgress(int p) {
        progress.setProgress(p);
        // TODO: 06/04/18 use string resource when merged in dev
        progress.setText(String.format(Locale.ENGLISH, "%d/%d", p, progress.getMax()));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ThemeHelper th = ThemeHelper.getInstanceLoaded(getContext());


        View view = inflater.inflate(R.layout.bottom_sheet_delete_media, container, false);
        ButterKnife.bind(this, view);
        view.setBackgroundColor(th.getBackgroundColor());

        header.setBackgroundColor(th.getPrimaryColor());

        txtErrors.setTextColor(th.getTextColor());
        progress.setFinishedStrokeColor(th.getAccentColor());
        progress.setTextColor(th.getTextColor());
        return view;
    }

   /* @Override
    public void setupDialog(Dialog dialog, int style) {
        ThemeHelper th = ThemeHelper.getInstanceLoaded(getContext());


        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_delete_media, null, false);
        ButterKnife.bind(this, view);
        view.setBackgroundColor(th.getBackgroundColor());

        header.setBackgroundColor(th.getPrimaryColor());

        txtErrors.setTextColor(th.getTextColor());
        progress.setFinishedStrokeColor(th.getAccentColor());
        progress.setTextColor(th.getTextColor());
        progress.setMax(media.size());
        setProgress(0);

        dialog.setContentView(view);
    }*/

    private void showErrors(HashSet<String> errors) {
        StringBuilder b = new StringBuilder();
        b.append("<b>").append("Errors:").append("</b>").append("<br/>");

        for (String error : errors)
            b.append("<i>").append(" - ").append(error).append("</i>").append("<br/>");

        txtErrors.setText(StringUtils.html(b.toString()));
        txtErrors.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);


    }

    @OnClick(R.id.cancel_delete)
    void cancelDelete() {
        Log.wtf(TAG, "delete stop");
        cancelRequested = true;
        listener.onCompleted();
        dismiss();
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();

    }

    private void done() {
        setCancelable(true);
        listener.onCompleted();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments == null || (media = arguments.getParcelableArrayList(EXTRA_MEDIA)) == null) {
            done();
            return;
        }
        progress.setMax(media.size());
        setProgress(0);

        HashSet<String> errors = new HashSet<>(0);

        Disposable subscribe = MediaHelper.deleteMedia(getContext(), media)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .takeUntil(mediaBooleanPair -> !cancelRequested)
                .subscribe(
                        pair -> {
                            if (pair.second)
                                listener.onDeleted(pair.first);
                            else
                                errors.add(pair.first.getName());

                            setProgress((int) (progress.getProgress() + 1));
                        }, err -> {
                        },
                        () -> {
                            if (errors.size() > 0)
                                showErrors(errors);
                            done();
                        });
        disposable.add(subscribe);

        /*for (Media m : media) {
            if(cancelRequested) break;

            boolean deleteSuccess = MediaHelper.internalDeleteMedia(getContext(), m);
            if (deleteSuccess) {
                listener.onDeleted(m);
            } else {
                errors.add(m.getPath());
            }

            setProgress((int) (progress.getProgress() + 1));
        }*/
        /*if (errors.size() > 0)
            showErrors(errors);
        else dismiss();*/


    }
}
