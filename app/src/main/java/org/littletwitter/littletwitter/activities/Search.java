package org.littletwitter.littletwitter.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.configuration.SharedPrefs;
import org.littletwitter.littletwitter.configuration.URLSource;
import org.littletwitter.littletwitter.cookies.Keys;
import org.littletwitter.littletwitter.cookies.UniversalCookieJar;
import org.littletwitter.littletwitter.cookies.UniversalCookiePersistor;
import org.littletwitter.littletwitter.customadapters.SearchUserAutoCompleteAdapter;
import org.littletwitter.littletwitter.responses.ArrayServerResponse;
import org.littletwitter.littletwitter.responses.ServerResponse;
import org.littletwitter.littletwitter.responses.StringServerResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Search extends AppCompatActivity implements SearchUserAutoCompleteAdapter.ActivityAPI {

    private String selectedUserId;
    private TextView uid;
    private TextView name;
    private TextView email;
    private Button toggleFollow;
    private View controlCenter;
    private AutoCompleteTextView searchBar;
    private OkHttpClient client;

    private String unFollow = "UnFollow";
    private String follow = "Follow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        View v = findViewById(R.id.activity_search);
        searchBar = v.findViewById(R.id.search_bar);
        uid = v.findViewById(R.id.uid);
        name = v.findViewById(R.id.name);
        email = v.findViewById(R.id.email);
        controlCenter = v.findViewById(R.id.control_center);
        controlCenter.setVisibility(View.GONE);

        // Back button
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Network
        Button viewPosts = v.findViewById(R.id.view_posts);
        viewPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Search.this, UserPosts.class);
                i.putExtra("userId", selectedUserId);
                startActivity(i);
            }
        });
        toggleFollow = v.findViewById(R.id.toggle_follow);
        toggleFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Objects.equals(toggleFollow.getText().toString(), unFollow)) {
                    new UnFollowUserTask().execute();
                } else if (Objects.equals(toggleFollow.getText().toString(), follow)) {
                    new FollowUserTask().execute();
                } else {
                    //TODO
                }
            }
        });
        Button cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Search.this.finish();
            }
        });

        UniversalCookieJar persistentCookieJar = new UniversalCookieJar(new SetCookieCache(), new UniversalCookiePersistor(this, SharedPrefs.SHARED_PREFS_NAME));
        client = new OkHttpClient.Builder()
                .cookieJar(persistentCookieJar)
                .build();

        searchBar.setAdapter(new SearchUserAutoCompleteAdapter(this, client));
    }

    @Override
    public void onSearchResultClick(String uid, String name, String email) {
        controlCenter.setVisibility(View.GONE);
        selectedUserId = uid;
        this.uid.setText(uid);
        this.name.setText(name);
        this.email.setText(email);
        searchBar.dismissDropDown();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
        new GetFollowersTask().execute();
    }

    private void afterGetFollowees(JSONArray followees) {

        boolean followee = false;
        JSONObject a = null;

        for (int i = 0; i < followees.length(); i++) {
            try {
                a = (JSONObject) followees.get(i);
                if (Objects.equals(a.getString("uid"), selectedUserId)) {
                    followee = true;
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                //TODO
            }
        }

        if (followee) {
            toggleFollow.setText(unFollow);
        } else {
            toggleFollow.setText(follow);
        }

        controlCenter.setVisibility(View.VISIBLE);

    }

    private void toggleFollowButton(boolean followToUnFollow) {
        if (followToUnFollow) {
            toggleFollow.setText(unFollow);
        } else {
            toggleFollow.setText(follow);
        }
    }

    @Override
    public void onInvalidSession() {
        getSharedPreferences(SharedPrefs.SHARED_PREFS_NAME, MODE_PRIVATE).edit().remove(Keys.JSESSIONID).apply();
        startActivity(new Intent(this, Login.class));
    }

    private class GetFollowersTask extends AsyncTask<Void, Void, ServerResponse> {

        @Override
        protected ServerResponse doInBackground(Void... params) {
            try {
                RequestBody requestBody = new FormBody.Builder().build();
                Request request = new Request.Builder()
                        .url(URLSource.getFollowers())
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body().string();

                return new ArrayServerResponse(body);
            } catch (IOException | JSONException | NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ServerResponse response) {
            if (response == null) {
                Toast.makeText(Search.this, "Server error", Toast.LENGTH_SHORT).show();
            } else {
                if (response.getStatus()) {
                    ArrayServerResponse a = (ArrayServerResponse) response;
                    afterGetFollowees(a.getData());
                } else {
                    Toast.makeText(Search.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    if (response.getErrorMessage().equalsIgnoreCase("Invalid session")) {
                        Search.this.getSharedPreferences(SharedPrefs.SHARED_PREFS_NAME, MODE_PRIVATE).edit().remove(Keys.JSESSIONID).apply();
                        startActivity(new Intent(Search.this, Login.class));
                    }
                }
            }
        }

    }

    private class FollowUserTask extends AsyncTask<Void, Void, ServerResponse> {

        @Override
        protected ServerResponse doInBackground(Void... params) {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("uid", URLEncoder.encode(selectedUserId, "utf-8"))
                        .build();

                Request request = new Request.Builder()
                        .url(URLSource.followUser())
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body().string();

                return new ArrayServerResponse(body);
            } catch (IOException | JSONException | NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ServerResponse response) {
            if (response == null) {
                Toast.makeText(Search.this, "Server error", Toast.LENGTH_SHORT).show();
            } else {
                if (response.getStatus()) {
                    // Got Response
                    Toast.makeText(Search.this, "You are following the user now!", Toast.LENGTH_SHORT).show();
                    toggleFollowButton(false);
                } else {
                    Toast.makeText(Search.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    if (response.getErrorMessage().equalsIgnoreCase("Invalid session")) {
                        Search.this.getSharedPreferences(SharedPrefs.SHARED_PREFS_NAME, MODE_PRIVATE).edit().remove(Keys.JSESSIONID).apply();
                        startActivity(new Intent(Search.this, Login.class));
                    }
                }
            }
        }

    }

    private class UnFollowUserTask extends AsyncTask<Void, Void, ServerResponse> {

        @Override
        protected ServerResponse doInBackground(Void... params) {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("uid", URLEncoder.encode(selectedUserId, "utf-8"))
                        .build();

                Request request = new Request.Builder()
                        .url(URLSource.unFollowUser())
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body().string();

                return new ArrayServerResponse(body);
            } catch (IOException | JSONException | NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ServerResponse response) {
            if (response == null) {
                Toast.makeText(Search.this, "Server error", Toast.LENGTH_SHORT).show();
            } else {
                if (response.getStatus()) {
                    //Got Response
                    Toast.makeText(Search.this, "You have un-followed the user!", Toast.LENGTH_SHORT).show();
                    toggleFollowButton(true);
                } else {
                    Toast.makeText(Search.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    if (response.getErrorMessage().equalsIgnoreCase("Invalid session")) {
                        Search.this.getSharedPreferences(SharedPrefs.SHARED_PREFS_NAME, MODE_PRIVATE).edit().remove(Keys.JSESSIONID).apply();
                        startActivity(new Intent(Search.this, Login.class));
                    }
                }
            }
        }

    }

}
