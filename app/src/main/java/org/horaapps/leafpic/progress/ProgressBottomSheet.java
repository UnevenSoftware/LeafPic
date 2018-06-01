package org.horaapps.leafpic.progress;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ThemeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dnld on 11/03/18.
 */

public class ProgressBottomSheet<T> extends BottomSheetDialogFragment {


    /**
     * Interface for listeners to get callbacks when items are processed.
     */
    public interface Listener<T> {
        void onCompleted();

        void onProgress(T item);
    }

    public static class Builder<T> {
        @StringRes
        int title;
        boolean showCancel = true;
        boolean autoDismiss = false;
        List<? extends ObservableSource<? extends T>> sources;
        Listener<T> listener;

        public Builder(int title) {
            this.title = title;
        }

        public Builder<T> showCancel(boolean showCancel) {
            this.showCancel = showCancel;
            return this;
        }

        public Builder<T> autoDismiss(boolean autoDismiss) {
            this.autoDismiss = autoDismiss;
            return this;
        }

        public Builder<T> sources(List<? extends ObservableSource<? extends T>> sources) {
            this.sources = sources;
            return this;
        }

        public Builder<T> listener(Listener<T> listener) {
            this.listener = listener;
            return this;
        }

        public ProgressBottomSheet<T> build() {
            if (sources == null)
                throw new RuntimeException("You must pass a list of observables");

            if (listener == null)
                Log.w(TAG, "You have not set a listener");

            ProgressBottomSheet<T> bottomSheet = new ProgressBottomSheet<>();

            bottomSheet.setTitle(title);
            bottomSheet.setAutoDismiss(autoDismiss);
            bottomSheet.setShowCancel(showCancel);
            bottomSheet.setSources(sources);
            bottomSheet.setListener(listener);
            return bottomSheet;

        }
    }

    private static final String TAG = "ProgressBottomSheet";

    @BindView(R.id.progress_header)
    ViewGroup headerLayout;
    @BindView(R.id.progress_progress_bar)
    DonutProgress progressBar;
    @BindView(R.id.progress_errors)
    RecyclerView rvErrors;
    @BindView(R.id.progress_title)
    TextView txtTitle;
    @BindView(R.id.progress_done_cancel_sheet)
    AppCompatButton btnDoneCancel;

    private Disposable disposable;
    private boolean done = false;
    @StringRes
    int title;
    boolean showCancel = true;
    boolean autoDismiss = false;
    List<? extends ObservableSource<? extends T>> sources;
    Listener<T> listener;


    public void setTitle(int title) {
        this.title = title;
    }

    public void setShowCancel(boolean showCancel) {
        this.showCancel = showCancel;
    }

    public void setAutoDismiss(boolean autoDismiss) {
        this.autoDismiss = autoDismiss;
    }

    public void setSources(List<? extends ObservableSource<? extends T>> sources) {
        this.sources = sources;
    }

    public void setListener(Listener<T> listener) {
        this.listener = listener;
    }

    private void setProgress(int progressPercent) {
        progressBar.setProgress(progressPercent);
        progressBar.setText(getString(R.string.toolbar_selection_count, progressPercent, progressBar.getMax()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_progress, container, false);
    }

    private void showErrors(List<ErrorCause> errors) {
        ErrorCauseAdapter adapter = new ErrorCauseAdapter(getContext(), errors);
        rvErrors.setLayoutManager(new LinearLayoutManager(getContext()));
        rvErrors.setAdapter(adapter);
        rvErrors.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void showErrors(CompositeException exceptions) {
        ArrayList<ErrorCause> errors = new ArrayList<>(exceptions.size());
        for (Throwable throwable : exceptions.getExceptions())
            errors.add(ErrorCause.fromThrowable(throwable));

        showErrors(errors);
    }

    private void showErrors(Throwable exception) {
        showErrors(Collections.singletonList(ErrorCause.fromThrowable(exception)));
    }

    @OnClick(R.id.progress_done_cancel_sheet)
    void cancel() {
        if (done) {
            dismiss();
        } else {
            if (disposable != null && !disposable.isDisposed()) disposable.dispose();
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
        btnDoneCancel.setVisibility(View.VISIBLE);
        setCancelable(true);
        listener.onCompleted();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setupViews(view);

        progressBar.setMax(sources.size());
        setProgress(0);


        disposable = Observable.mergeDelayError(sources)
                .observeOn(AndroidSchedulers.mainThread(), true)
                .subscribeOn(Schedulers.newThread())
                .doFinally(() -> {
                    done();
                    if (autoDismiss)
                        dismiss();
                })
                .subscribe(item -> {
                    listener.onProgress(item);
                    setProgress((int) (progressBar.getProgress() + 1));
                }, err -> {
                    // Note: progress is useless here since errors are delayed
                    if (err instanceof CompositeException) showErrors(((CompositeException) err));
                    else showErrors(err);
                });
    }

    private void setupViews(@NonNull View view) {
        ThemeHelper th = ThemeHelper.getInstanceLoaded(getContext());
        view.setBackgroundColor(th.getBackgroundColor());
        headerLayout.setBackgroundColor(th.getPrimaryColor());
        progressBar.setFinishedStrokeColor(th.getAccentColor());
        progressBar.setTextColor(th.getTextColor());
        txtTitle.setText(title);
        if (!showCancel) btnDoneCancel.setVisibility(View.GONE);
    }
}
