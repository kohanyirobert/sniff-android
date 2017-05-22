package com.github.kohanyirobert.sniff.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.github.kohanyirobert.sniff.fragment.MainFragment;
import com.github.kohanyirobert.sniff.fragment.SettingsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_KEY;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_URL;

public class MainActivity extends AppCompatActivity implements MainFragment.SendClickListener {

    private static final String TAG = MainActivity.class.getName();

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
        if (apiUrl == null || apiKey == null) {
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
    public void onSendClicked(Map<String, String> tags, final MainFragment.SendDoneListener done) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String apiUrl = preferences.getString(API_URL, null);
        final String apiKey = preferences.getString(API_KEY, null);
        JSONObject postBody = new JSONObject();
        try {
            postBody.put("url", mParameters.getNormalizedUrl());
            postBody.put("tags", tags);
        } catch (JSONException e) {
            throw new IllegalArgumentException();
        }
        Log.v(TAG, String.format("Sending JSON: %s", postBody));
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                done.onSendDone(getResources().getString(R.string.ok));
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                done.onSendDone(getResources().getString(R.string.error));
            }
        };
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, apiUrl, postBody, listener, errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-api-key", apiKey);
                return headers;
            }
        };
        mRequestQueue.add(request);
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
                .replace(R.id.frame_layout_main, MainFragment.create(mParameters))
                .commit();
    }
}
