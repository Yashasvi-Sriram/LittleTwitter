package org.littletwitter.littletwitter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyPosts extends AppCompatActivity {
    private RecyclerView postListView;
    private SwipeRefreshLayout postListSwipeRefreshLayout;
    private PostListAdapter postListAdapter;
    private boolean fetchingPostBatch = false;

    private int offset;
    private final int limit = 10;
    private OkHttpClient client;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        //back button
        if(getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);

        // UI
        final View v = findViewById(R.id.activity_my_posts);
        postListView = v.findViewById(R.id.post_list);
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
                        Snackbar.make(postListView, "Getting more posts", Snackbar.LENGTH_SHORT).show();
                        fetchNextBatchOfPosts();
                    }
                }
            }
        });

        postListSwipeRefreshLayout = v.findViewById(R.id.post_list_swipe_refresh_layout);
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

    private void startFromFirstBatch() {
        offset = 0;
        new PostFetchTask().execute();
    }

    private void fetchNextBatchOfPosts() {
        new PostFetchTask().execute();
    }

    private class PostFetchTask extends AsyncTask<Void, Void, ServerResponse> {
        @Override
        protected ServerResponse doInBackground(Void... params) {
            try {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URLSource.seeMyPosts()).newBuilder()
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
                Toast.makeText(MyPosts.this, "Server error", Toast.LENGTH_SHORT).show();
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
                            postListAdapter = new PostListAdapter(posts, MyPosts.this, client);
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
                                Snackbar.make(postListView, "End of posts!", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                        offset += limit;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MyPosts.this, "Client doesn't seem to know server well", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyPosts.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    if (response.getErrorMessage().equalsIgnoreCase("Invalid session")) {
                        sp.edit().remove(Keys.JSESSIONID).apply();
                        startActivity(new Intent(MyPosts.this, Login.class));
                    }
                }
            }
            fetchingPostBatch = false;
        }
    }

}
