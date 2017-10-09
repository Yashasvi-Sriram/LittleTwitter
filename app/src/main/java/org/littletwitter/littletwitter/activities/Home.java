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

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private OkHttpClient client;
    private View progressView;
    private ImageView profilePictureView;
    private TextView userIdView;
    private RecyclerView postListView;
    private SwipeRefreshLayout postListSwipeRefreshLayout;
    private PostListAdapter postListAdapter;
    private boolean fetchingPostBatch = false;
    private SharedPreferences sp;

    private int offset;
    private final int limit = 10;

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

        View contentHome = findViewById(R.id.content_home);
        postListView = contentHome.findViewById(R.id.post_list);
        final LinearLayoutManager postListViewLayoutManager = new LinearLayoutManager(this);
        postListView.setLayoutManager(postListViewLayoutManager);
        postListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = postListViewLayoutManager.getChildCount();
                    int totalItemCount = postListViewLayoutManager.getItemCount();
                    int pastVisibleItems = postListViewLayoutManager.findFirstVisibleItemPosition();
                    if (!fetchingPostBatch && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        fetchingPostBatch = true;
                        // dirty line
                        Snackbar.make(progressView, "Getting more posts", Snackbar.LENGTH_SHORT).show();
                        fetchNextBatchOfPosts();
                    }
                }
            }
        });

        postListSwipeRefreshLayout = contentHome.findViewById(R.id.post_list_swipe_refresh_layout);
        postListSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startFromFirstBatch();
            }
        });
        postListSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Network
        UniversalCookieJar persistentCookieJar = new UniversalCookieJar(new SetCookieCache(), new UniversalCookiePersistor(this, SharedPrefs.SHARED_PREFS_NAME));
        client = new OkHttpClient.Builder()
                .cookieJar(persistentCookieJar)
                .build();

        // Shared Preferences
        sp = getSharedPreferences(SharedPrefs.SHARED_PREFS_NAME, MODE_PRIVATE);

        // Init
        startFromFirstBatch();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
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

    private void startFromFirstBatch() {
        offset = 0;
        new PostFetchTask().execute();
    }

    private void fetchNextBatchOfPosts() {
        new PostFetchTask().execute();
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

    private class PostFetchTask extends AsyncTask<Void, Void, ServerResponse> {
        @Override
        protected ServerResponse doInBackground(Void... params) {
            try {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URLSource.seePosts()).newBuilder()
                        .addQueryParameter("offset", String.valueOf(offset))
                        .addQueryParameter("limit", String.valueOf(limit));

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
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
            postListSwipeRefreshLayout.setRefreshing(false);
            if (response == null) {
                Toast.makeText(Home.this, "Server error", Toast.LENGTH_SHORT).show();
            } else {
                if (response.getStatus()) {
                    try {
                        ArrayServerResponse a = (ArrayServerResponse) response;
                        List<Post> posts = new ArrayList<>();
                        for (int i = 0; i < a.getData().length(); i++) {
                            Post post = new Post(a.getData().getJSONObject(i));
                            posts.add(post);
                        }
                        // reset post list view
                        if (offset == 0) {
                            postListAdapter = new PostListAdapter(posts, Home.this, client);
                            postListView.setAdapter(postListAdapter);
                        } else {
                            // append new posts at the bottom
                            for (Post newPost : posts) {
                                postListAdapter.add(newPost);
                            }
                            // last batch
                            if (a.getData().length() < limit) {
                                offset -= limit;
                                offset += a.getData().length();
                            }
                            if (a.getData().length() == 0) {
                                // dirty line
                                Snackbar.make(progressView, "End of posts!", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                        offset += limit;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Home.this, "Client doesn't seem to know server well", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Home.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    if (response.getErrorMessage().equalsIgnoreCase("Invalid session")) {
                        sp.edit().remove(Keys.JSESSIONID).apply();
                        startActivity(new Intent(Home.this, Login.class));
                    }
                }
            }
            fetchingPostBatch = false;
        }
    }

}
