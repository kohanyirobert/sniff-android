package com.github.kohanyirobert.sniff.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.Volley;
import com.github.kohanyirobert.sniff.R;
import com.github.kohanyirobert.sniff.Utils;
import com.github.kohanyirobert.sniff.fragment.MainFragment;
import com.github.kohanyirobert.sniff.fragment.SettingsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static androidx.fragment.app.FragmentManager.OnBackStackChangedListener;
import static com.github.kohanyirobert.sniff.fragment.MainFragment.SendClickListener;
import static com.github.kohanyirobert.sniff.fragment.MainFragment.SendDoneListener;
import static com.github.kohanyirobert.sniff.fragment.MainFragment.SendStatus.CANCELLED;
import static com.github.kohanyirobert.sniff.fragment.MainFragment.SendStatus.FAILED;
import static com.github.kohanyirobert.sniff.fragment.MainFragment.SendStatus.SUCCESSFUL;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_KEY;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_URL;
import static java.lang.String.format;

public class MainActivity extends AppCompatActivity implements OnBackStackChangedListener, SendClickListener {

    public static final String EXTRA_MODE = MainActivity.class.getPackage().getName() + ".EXTRA_MODE";

    private static final String TAG = MainActivity.class.getName();

    private MainActivityParameters mParameters;
    private RequestQueue mRequestQueue;
    private MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mParameters = MainActivityParameters.create(getIntent());
        mRequestQueue = getRequestQueue();
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState == null) {
            mMainFragment = MainFragment.create(mParameters);
        } else {
            mMainFragment = (MainFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, MainFragment.class.getName());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        shouldDisplayHomeUp();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout_main, mMainFragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, MainFragment.class.getName(), mMainFragment);
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                showFragmentSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSendClicked(Map<String, String> tags, SendDoneListener done) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String apiUrl = preferences.getString(API_URL, null);
        String apiKey = preferences.getString(API_KEY, null);
        int messageId = validateApiUrlAndKey(apiUrl, apiKey);
        if (messageId == 0) {
            sendPostBody(apiUrl, apiKey, createPostBody(tags), done);
        } else {
            final CoordinatorLayout parent = (CoordinatorLayout) findViewById(R.id.coordinator_layout_main);
            final Snackbar snackbar = Snackbar.make(parent, messageId, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.settings, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFragmentSettings();
                }
            });
            snackbar.show();
            done.onSendDone(CANCELLED, null);
        }
    }

    @Override
    public void onSendFinished() {
        MainActivity.this.finish();
    }

    private void shouldDisplayHomeUp() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
    }

    private int validateApiUrlAndKey(String apiUrl, String apiKey) {
        if (TextUtils.isEmpty(apiUrl) && TextUtils.isEmpty(apiKey)) {
            return R.string.missing_api_url_and_api_key;
        }

        if (TextUtils.isEmpty(apiUrl)) {
            return R.string.missing_api_url;
        }

        if (TextUtils.isEmpty(apiKey)) {
            return R.string.missing_api_key;
        }

        if (!Utils.isApiUrlValid(apiUrl) && !Utils.isApiKeyValid(apiKey)) {
            return R.string.invalid_api_url_and_api_key;
        }

        if (!Utils.isApiUrlValid(apiUrl)) {
            return R.string.invalid_api_url;
        }

        if (!Utils.isApiKeyValid(apiKey)) {
            return R.string.invalid_api_key;
        }

        return 0;
    }

    private RequestQueue getRequestQueue() {
        RequestQueue queue;
        if (mParameters.getMode() == MainActivityMode.TEST) {
            queue = new RequestQueue(new NoCache(), new Network() {
                @Override
                public NetworkResponse performRequest(Request<?> request) throws VolleyError {
                    return new NetworkResponse(null);
                }
            });
            queue.start();
        } else {
            queue = Volley.newRequestQueue(this);
        }
        return queue;
    }

    private void showFragmentSettings() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout_main, new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    @NonNull
    private JSONObject createPostBody(Map<String, String> tags) {
        JSONObject postBody = new JSONObject();
        try {
            postBody.put("url", mParameters.getNormalizedUrl());
            postBody.put("tags", new JSONObject(tags));
        } catch (JSONException e) {
            throw new IllegalArgumentException();
        }
        return postBody;
    }

    private void sendPostBody(final String apiUrl, final String apiKey, final JSONObject postBody, final SendDoneListener done) {
        Log.v(TAG, format("request: %s%n", postBody.toString()));
        mRequestQueue.add(new JsonRequest<String>(Request.Method.POST, apiUrl, postBody.toString(), null, null) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                final String message = response.data == null ? null : new String(response.data);
                if (response.statusCode >= 200 && response.statusCode < 300) {
                    Log.v(TAG, format("status: %s, response: %s%n", response.statusCode, message));
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            done.onSendDone(SUCCESSFUL, getResources().getString(R.string.send_request_success));
                        }
                    });
                } else {
                    Log.e(TAG, format("status: %s, response: %s%n", response.statusCode, message));
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            done.onSendDone(FAILED, getResources().getString(R.string.send_request_failure));
                        }
                    });
                }
                return null;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-api-key", apiKey);
                return headers;
            }
        });
    }
}
