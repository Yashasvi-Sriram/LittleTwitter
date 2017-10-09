package org.littletwitter.littletwitter.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.beans.Post;
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

public class InfinitePostListFragment extends Fragment {

    private final int limit = 10;
    private int offset;
    private boolean fetchingPostBatch = false;
    private View rootView;
    private RecyclerView postListView;
    private PostListAdapter postListAdapter;
    private SwipeRefreshLayout postListSwipeRefreshLayout;
    private Context context;
    private ActivityAPI activityAPI;
    private OkHttpClient client;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater layoutInflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = layoutInflater.inflate(R.layout.infinite_post_list_fragment, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // UI
        postListView = rootView.findViewById(R.id.post_list);
        final LinearLayoutManager postListViewLayoutManager = new LinearLayoutManager(context);
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
                        Snackbar.make(postListView, "Getting more posts", Snackbar.LENGTH_SHORT).show();
                        fetchNextBatchOfPosts();
                    }
                }
            }
        });

        postListSwipeRefreshLayout = rootView.findViewById(R.id.post_list_swipe_refresh_layout);
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

        // Interface
        activityAPI = (ActivityAPI) context;

        // Network
        client = activityAPI.getClient();

        // Init
        Snackbar.make(postListView, "Getting posts", Snackbar.LENGTH_SHORT).show();
        startFromFirstBatch();
    }

    public interface ActivityAPI {
        public void onInvalidSession();

        public OkHttpClient getClient();

        public String getCompleteURL();

        public List<Pair<String, String>> getURLParameters();
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
                HttpUrl.Builder urlBuilder = HttpUrl.parse(activityAPI.getCompleteURL()).newBuilder()
                        .addQueryParameter("offset", String.valueOf(offset))
                        .addQueryParameter("limit", String.valueOf(limit));

                List<Pair<String, String>> extraParams = activityAPI.getURLParameters();
                for (Pair<String, String> param : extraParams) {
                    urlBuilder.addQueryParameter(param.first, param.second);
                }

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
                Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
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
                            postListAdapter = new PostListAdapter(posts, context, client);
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
                                Snackbar.make(postListView, "End of posts!", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                        offset += limit;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Client doesn't seem to know server well", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    if (response.getErrorMessage().equalsIgnoreCase("Invalid session")) {
                        activityAPI.onInvalidSession();
                    }
                }
            }
            fetchingPostBatch = false;
        }


    }


}
