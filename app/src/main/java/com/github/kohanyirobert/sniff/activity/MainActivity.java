package com.github.kohanyirobert.sniff.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.Volley;
import com.github.kohanyirobert.sniff.R;
import com.github.kohanyirobert.sniff.Utils;
import com.github.kohanyirobert.sniff.fragment.SettingsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.github.kohanyirobert.sniff.fragment.MainFragment.SendClickListener;
import static com.github.kohanyirobert.sniff.fragment.MainFragment.SendDoneListener;
import static com.github.kohanyirobert.sniff.fragment.MainFragment.create;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_KEY;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_URL;

public class MainActivity extends AppCompatActivity implements SendClickListener {

    public static final String EXTRA_MODE = MainActivity.class.getPackage().getName() + ".EXTRA_MODE";

    private MainActivityParameters mParameters;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mParameters = MainActivityParameters.create(getIntent());
        mRequestQueue = getRequestQueue();
        showFragmentMain();
    }

    private RequestQueue getRequestQueue() {
        RequestQueue queue;
        if (mParameters.getMode() == MainActivityMode.TEST) {
            queue = new RequestQueue(new NoCache(), new Network() {
                @Override
                public NetworkResponse performRequest(Request<?> request) throws VolleyError {
                    return new NetworkResponse(new JSONObject().toString().getBytes(StandardCharsets.UTF_8));
                }
            });
            queue.start();
        } else {
            queue = Volley.newRequestQueue(this);
        }
        return queue;
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
        }
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

    @Override
    public void onSendFinished() {
        MainActivity.this.finish();
    }

    private void showFragmentSettings() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout_main, new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    private int showFragmentMain() {
        return getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout_main, create(mParameters))
                .commit();
    }

    @NonNull
    private JSONObject createPostBody(Map<String, String> tags) {
        JSONObject postBody = new JSONObject();
        try {
            postBody.put("url", mParameters.getNormalizedUrl());
            postBody.put("tags", tags);
        } catch (JSONException e) {
            throw new IllegalArgumentException();
        }
        return postBody;
    }

    private void sendPostBody(final String apiUrl, final String apiKey, final JSONObject postBody, final SendDoneListener done) {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                done.onSendDone(getResources().getString(R.string.send_request_success));
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                done.onSendDone(error.getMessage());
            }
        };
        mRequestQueue.add(new JsonObjectRequest(Request.Method.POST, apiUrl, postBody, listener, errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-api-key", apiKey);
                return headers;
            }
        });
    }
}
