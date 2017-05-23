package com.github.kohanyirobert.sniff.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.kohanyirobert.sniff.R;
import com.github.kohanyirobert.sniff.fragment.SettingsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.github.kohanyirobert.sniff.fragment.MainFragment.SendClickListener;
import static com.github.kohanyirobert.sniff.fragment.MainFragment.SendDoneListener;
import static com.github.kohanyirobert.sniff.fragment.MainFragment.create;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_KEY;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_URL;

public class MainActivity extends AppCompatActivity implements SendClickListener {

    private MainActivityParameters mParameters;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mParameters = MainActivityParameters.create(getIntent());
        mRequestQueue = Volley.newRequestQueue(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String apiUrl = preferences.getString(API_URL, null);
        String apiKey = preferences.getString(API_KEY, null);
        if (TextUtils.isEmpty(apiUrl) || TextUtils.isEmpty(apiKey)) {
            showFragmentMain();
            showFragmentSettings();
        } else {
            showFragmentMain();
        }
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
        if (apiUrl == null && apiKey == null) {
            done.onSendDone(getResources().getString(R.string.required_settings_missing));
        } else {
            sendPostBody(apiUrl, apiKey, createPostBody(tags), done);
        }
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
                done.onSendDone(getResources().getString(R.string.send_request_failure));
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
