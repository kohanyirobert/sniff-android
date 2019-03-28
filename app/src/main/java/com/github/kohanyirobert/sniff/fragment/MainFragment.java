package com.github.kohanyirobert.sniff.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.github.kohanyirobert.sniff.R;
import com.github.kohanyirobert.sniff.activity.MainActivityParameters;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MainFragment extends Fragment {

    @SuppressWarnings("unused")
    public static final class SnackbarBehavior extends CoordinatorLayout.Behavior<ConstraintLayout> {

        public SnackbarBehavior(Context context, AttributeSet attrs) {
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, final ConstraintLayout child, final View dependency) {
            final float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
            child.findViewById(R.id.floating_button_send).setTranslationY(translationY);
            return true;
        }

        @Override
        public void onDependentViewRemoved(CoordinatorLayout parent, ConstraintLayout child, View dependency) {
            child.findViewById(R.id.floating_button_send)
                    .animate()
                    .setStartDelay(0L)
                    .setDuration(100L)
                    .translationY(0.0f)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, ConstraintLayout child, View dependency) {
            return dependency instanceof Snackbar.SnackbarLayout;
        }
    }

    private static final String FOCUSED_ID = "FOCUSED_ID";
    private static final String FOCUS_SELECTION_START = "FOCUS_SELECTION_START";
    private static final String FOCUS_SELECTION_END = "FOCUS_SELECTION_END";

    private static final String ARTIST = "ARTIST";
    private static final String TITLE = "TITLE";

    public static MainFragment create(MainActivityParameters mParameters) {
        Bundle args = new Bundle();
        args.putParcelable(MainActivityParameters.class.getName(), mParameters);
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public enum SendStatus {

        SUCCESSFUL,
        CANCELLED,
        FAILED
    }

    public interface SendDoneListener {

        void onSendDone(SendStatus status, String message);
    }

    public interface SendClickListener {

        void onSendClicked(Map<String, String> tags, SendDoneListener done);

        void onSendFinished();
    }

    private final class CheckHasErrorOnFocusChangeListener implements View.OnFocusChangeListener {
        private final TextInputLayout mInputLayout;
        private final TextInputEditText mEditText;
        private final int mMessageId;

        CheckHasErrorOnFocusChangeListener(TextInputLayout inputLayout, TextInputEditText editText, int messageId) {
            mInputLayout = inputLayout;
            mEditText = editText;
            mMessageId = messageId;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            checkHasError(mInputLayout, mEditText, mMessageId);
        }
    }

    private MainActivityParameters mMainActivityParameters;
    private SendClickListener mSendListener;

    private CoordinatorLayout mMainCoordinatorLayout;
    private ConstraintLayout mMainConstraintLayout;
    private TextView mVideoTitleTextView;
    private TextInputLayout mArtistInputLayout;
    private TextInputEditText mArtistEditText;
    private TextInputLayout mTitleInputLayout;
    private TextInputEditText mTitleEditText;
    private FloatingActionButton mSendFloatingButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivityParameters = getArguments().getParcelable(MainActivityParameters.class.getName());
        mSendListener = (SendClickListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        mMainCoordinatorLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_main, container, false);
        mMainConstraintLayout = (ConstraintLayout) mMainCoordinatorLayout.findViewById(R.id.constraint_layout_main);
        mVideoTitleTextView = (TextView) mMainConstraintLayout.findViewById(R.id.text_view_video_title);
        mArtistInputLayout = (TextInputLayout) mMainConstraintLayout.findViewById(R.id.input_layout_artist);
        mArtistEditText = (TextInputEditText) mMainConstraintLayout.findViewById(R.id.edit_text_artist);
        mTitleInputLayout = (TextInputLayout) mMainConstraintLayout.findViewById(R.id.input_layout_title);
        mTitleEditText = (TextInputEditText) mMainConstraintLayout.findViewById(R.id.edit_text_title);
        mSendFloatingButton = (FloatingActionButton) mMainConstraintLayout.findViewById(R.id.floating_button_send);

        mVideoTitleTextView.setText(mMainActivityParameters.getVideoTitle());
        mArtistEditText.setText(mMainActivityParameters.getArtist());
        mTitleEditText.setText(mMainActivityParameters.getTitle());

        checkHasError(mArtistInputLayout, mArtistEditText, R.string.required_artist);
        checkHasError(mTitleInputLayout, mTitleEditText, R.string.required_title);

        mArtistEditText.setOnFocusChangeListener(new CheckHasErrorOnFocusChangeListener(mArtistInputLayout, mArtistEditText, R.string.required_artist));
        mTitleEditText.setOnFocusChangeListener(new CheckHasErrorOnFocusChangeListener(mTitleInputLayout, mTitleEditText, R.string.required_title));

        mSendFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndFocusTagErrors()) {
                    return;
                }
                toggleEnabledOnViews();
                Map<String, String> tags = new LinkedHashMap<>();
                tags.put(ARTIST, mArtistEditText.getText().toString());
                tags.put(TITLE, mTitleEditText.getText().toString());
                mSendListener.onSendClicked(tags, new SendDoneListener() {
                    @Override
                    public void onSendDone(SendStatus status, String message) {
                        if (status == SendStatus.SUCCESSFUL) {
                            mSendFloatingButton.setImageResource(R.drawable.ic_done_white_18dp);
                        } else {
                            toggleEnabledOnViews();
                            mSendFloatingButton.setImageResource(R.drawable.ic_send_white_18dp);
                        }
                        if (status != SendStatus.CANCELLED) {
                            Snackbar snackbar = Snackbar.make(mMainCoordinatorLayout, message, Snackbar.LENGTH_SHORT);
                            if (status == SendStatus.SUCCESSFUL) {
                                snackbar.addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                        mSendListener.onSendFinished();
                                    }
                                });
                            }
                            snackbar.show();
                        }
                    }
                });
            }
        });
        return mMainCoordinatorLayout;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mArtistEditText.setText(savedInstanceState.getString(ARTIST));
            mTitleEditText.setText(savedInstanceState.getString(TITLE));
            int focusedId = savedInstanceState.getInt(FOCUSED_ID);
            if (focusedId != 0) {
                int selectionStart = savedInstanceState.getInt(FOCUS_SELECTION_START);
                int selectionEnd = savedInstanceState.getInt(FOCUS_SELECTION_END);
                EditText editText = (EditText) mMainCoordinatorLayout.findViewById(focusedId);
                editText.setSelection(selectionStart, selectionEnd);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARTIST, mArtistEditText.getText().toString());
        outState.putString(TITLE, mTitleEditText.getText().toString());

        View focusedView = getActivity().getCurrentFocus();
        if (focusedView instanceof TextInputEditText) {
            TextInputEditText editText = (TextInputEditText) focusedView;
            outState.putInt(FOCUSED_ID, editText.getId());
            outState.putInt(FOCUS_SELECTION_START, editText.getSelectionStart());
            outState.putInt(FOCUS_SELECTION_END, editText.getSelectionEnd());
        }
    }

    private void toggleEnabledOnViews() {
        mVideoTitleTextView.setEnabled(!mVideoTitleTextView.isEnabled());
        mArtistEditText.setEnabled(!mArtistEditText.isEnabled());
        mTitleEditText.setEnabled(!mTitleEditText.isEnabled());
        mSendFloatingButton.setEnabled(!mSendFloatingButton.isEnabled());
    }

    private boolean checkHasError(TextInputLayout inputLayout, EditText editText, int messageId) {
        if (TextUtils.isEmpty(editText.getText())) {
            inputLayout.setEnabled(true);
            inputLayout.setError(getResources().getString(messageId));
            return true;
        }
        inputLayout.setErrorEnabled(false);
        inputLayout.setError(null);
        return false;
    }

    private boolean checkAndFocusTagErrors() {
        boolean artistHasError = checkHasError(mArtistInputLayout, mArtistEditText, R.string.required_artist);
        boolean titleHasError = checkHasError(mTitleInputLayout, mTitleEditText, R.string.required_title);
        if (artistHasError || titleHasError) {
            if (artistHasError) {
                mArtistEditText.requestFocus();
            } else {
                mTitleEditText.requestFocus();
            }
            return true;
        }
        return false;
    }
}
