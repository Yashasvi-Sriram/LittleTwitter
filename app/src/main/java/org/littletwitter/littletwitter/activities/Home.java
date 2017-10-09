package org.littletwitter.littletwitter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.json.JSONException;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.beans.Post;
import org.littletwitter.littletwitter.configuration.SharedPrefs;
import org.littletwitter.littletwitter.configuration.URLSource;
import org.littletwitter.littletwitter.cookies.Keys;
import org.littletwitter.littletwitter.cookies.UniversalCookieJar;
import org.littletwitter.littletwitter.cookies.UniversalCookiePersistor;
import org.littletwitter.littletwitter.customadapters.PostListAdapter;
import org.littletwitter.littletwitter.fragments.InfinitePostListFragment;
import org.littletwitter.littletwitter.responses.ArrayServerResponse;
import org.littletwitter.littletwitter.responses.ServerResponse;
import org.littletwitter.littletwitter.responses.StringServerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, InfinitePostListFragment.ActivityAPI {

    private DrawerLayout drawerLayout;
    private OkHttpClient client;
    private View progressView;
    private ImageView profilePictureView;
    private TextView userIdView;
    private boolean fetchingPostBatch = false;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // UI
        Toolbar toolbar = findViewById(R.id.app_bar_home).findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.activity_home_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = drawerLayout.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        profilePictureView = (ImageView) findViewById(R.id.profile_picture);
        userIdView = (TextView) findViewById(R.id.user_id);

        progressView = findViewById(R.id.activity_home).findViewById(R.id.logout_progress);

        // Network
        UniversalCookieJar persistentCookieJar = new UniversalCookieJar(new SetCookieCache(), new UniversalCookiePersistor(this, SharedPrefs.SHARED_PREFS_NAME));
        client = new OkHttpClient.Builder()
                .cookieJar(persistentCookieJar)
                .build();

        // Shared Preferences
        sp = getSharedPreferences(SharedPrefs.SHARED_PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.add_post:
                startActivity(new Intent(this, AddPost.class));
                break;
            case R.id.my_posts:
                startActivity(new Intent(Home.this, MyPosts.class));
                break;
            case R.id.search:
                startActivity(new Intent(Home.this, Search.class));
                break;
            case R.id.log_out:
                logout();
                break;
            default:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        new UserLogoutTask().execute();
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        drawerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        drawerLayout.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                drawerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onInvalidSession() {
        sp.edit().remove(Keys.JSESSIONID).apply();
        startActivity(new Intent(this, Login.class));
    }

    @Override
    public OkHttpClient getClient() {
        return client;
    }

    @Override
    public String getCompleteURL() {
        return URLSource.seePosts();
    }

    @Override
    public List<Pair<String, String>> getURLParameters() {
        return new ArrayList<>();
    }

    private class UserLogoutTask extends AsyncTask<Void, Void, ServerResponse> {
        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected ServerResponse doInBackground(Void... params) {
            try {
                Request request = new Request.Builder()
                        .url(URLSource.logout())
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body().string();

                return new StringServerResponse(body);
            } catch (IOException | JSONException | NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ServerResponse response) {
            showProgress(false);
            if (response == null) {
                Toast.makeText(Home.this, "Server error", Toast.LENGTH_SHORT).show();
            } else {
                if (!response.getStatus()) {
                    Toast.makeText(Home.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            sp.edit().remove(Keys.JSESSIONID).apply();
            startActivity(new Intent(Home.this, Login.class));
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }

}
