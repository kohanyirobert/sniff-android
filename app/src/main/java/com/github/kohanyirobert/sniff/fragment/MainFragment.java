package com.github.kohanyirobert.sniff.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        public boolean onDependentViewChanged(CoordinatorLayout parent, ConstraintLayout child, View dependency) {
            float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
            child.findViewById(R.id.floating_button_send).setTranslationY(translationY);
            return true;
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, ConstraintLayout child, View dependency) {
            return dependency instanceof Snackbar.SnackbarLayout;
        }
    }

    private static final String ARTIST = "ARTIST";
    private static final String TITLE = "TITLE";

    public static MainFragment create(MainActivityParameters mParameters) {
        Bundle args = new Bundle();
        args.putParcelable(MainActivityParameters.class.getName(), mParameters);
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface SendDoneListener {

        void onSendDone(String message);
    }

    public interface SendClickListener {

        void onSendClicked(Map<String, String> tags, SendDoneListener listener);

        void onSendFinished();
    }

    private MainActivityParameters mMainActivityParameters;
    private SendClickListener mSendListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivityParameters = getArguments().getParcelable(MainActivityParameters.class.getName());
        mSendListener = (SendClickListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final CoordinatorLayout mainCoordinatorLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_main, container, false);
        final ConstraintLayout mainConstraintLayout = (ConstraintLayout) mainCoordinatorLayout.findViewById(R.id.constraint_layout_main);
        final TextView videoTitleTextView = (TextView) mainConstraintLayout.findViewById(R.id.text_view_video_title);
        final EditText artistEditText = (EditText) mainConstraintLayout.findViewById(R.id.edit_text_artist);
        final EditText titleEditText = (EditText) mainConstraintLayout.findViewById(R.id.edit_text_title);
        final FloatingActionButton sendFloatingButton = (FloatingActionButton) mainConstraintLayout.findViewById(R.id.floating_button_send);

        videoTitleTextView.setText(mMainActivityParameters.getVideoTitle());
        artistEditText.setText(mMainActivityParameters.getArtist());
        titleEditText.setText(mMainActivityParameters.getTitle());

        sendFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoTitleTextView.setEnabled(false);
                artistEditText.setEnabled(false);
                titleEditText.setEnabled(false);
                sendFloatingButton.setEnabled(false);
                Map<String, String> tags = new LinkedHashMap<>();
                tags.put(ARTIST, artistEditText.getText().toString());
                tags.put(TITLE, titleEditText.getText().toString());
                mSendListener.onSendClicked(tags, new SendDoneListener() {
                    @Override
                    public void onSendDone(String message) {
                        sendFloatingButton.setImageResource(R.drawable.ic_done_white_18dp);
                        Snackbar snackbar = Snackbar.make(mainCoordinatorLayout, R.string.ok, Snackbar.LENGTH_SHORT);
                        snackbar.addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                mSendListener.onSendFinished();
                            }
                        });
                        snackbar.show();
                    }
                });
            }
        });
        return mainCoordinatorLayout;
    }
}
