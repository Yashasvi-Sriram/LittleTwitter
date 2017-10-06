package org.littletwitter.littletwitter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
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
import org.json.JSONObject;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.beans.Post;
import org.littletwitter.littletwitter.cookies.UniversalCookieJar;
import org.littletwitter.littletwitter.customadapters.PostAdapter;
import org.littletwitter.littletwitter.responses.ArrayServerResponse;
import org.littletwitter.littletwitter.responses.ServerResponse;
import org.littletwitter.littletwitter.responses.StringServerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private OkHttpClient client;
    private View progressView;
    private ImageView profilePicture;
    private TextView userId;
    private RecyclerView postListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // UI
        Toolbar toolbar = findViewById(R.id.app_bar_home).findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.app_bar_home).findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Implement add post", Snackbar.LENGTH_SHORT).show();
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.activity_home_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = drawerLayout.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        profilePicture = (ImageView) findViewById(R.id.profile_picture);
        userId = (TextView) findViewById(R.id.user_id);
        progressView = findViewById(R.id.activity_home).findViewById(R.id.logout_progress);

        postListView = findViewById(R.id.content_home).findViewById(R.id.post_list);
        /**/
        List<Post> posts = new ArrayList<>();
        try {
            ArrayServerResponse a = new ArrayServerResponse("{\"data\":[{\"uid\":\"00128\",\"Comment\":[],\"postid\":32,\"text\":\"Hi! this is Zhang!!\",\"timestamp\":\"2017-10-05 13:10:08.814515\"},{\"uid\":\"00128\",\"Comment\":[],\"postid\":33,\"text\":\"I live in Mumbai.\",\"timestamp\":\"2017-10-05 13:10:08.833531\"},{\"uid\":\"00128\",\"Comment\":[],\"postid\":34,\"text\":\"I like to sleep and eat\",\"timestamp\":\"2017-10-05 13:10:08.861082\"},{\"uid\":\"00128\",\"Comment\":[],\"postid\":35,\"text\":\"I dont study much\",\"timestamp\":\"2017-10-05 13:10:08.878011\"},{\"uid\":\"00128\",\"Comment\":[],\"postid\":36,\"text\":\"I play a lot of games\",\"timestamp\":\"2017-10-05 13:10:08.922715\"},{\"uid\":\"12345\",\"Comment\":[],\"postid\":37,\"text\":\"Hi! This is Shankar!!\",\"timestamp\":\"2017-10-05 13:10:08.946343\"},{\"uid\":\"12345\",\"Comment\":[{\"uid\":\"12345\",\"name\":\"Shankar\",\"text\":\"This is a comment!\",\"timestamp\":\"2017-10-06 20:56:41.340249\"}],\"postid\":38,\"text\":\"I live in Delhi\",\"timestamp\":\"2017-10-05 13:10:09.000018\"},{\"uid\":\"12345\",\"Comment\":[],\"postid\":39,\"text\":\"I like to play games\",\"timestamp\":\"2017-10-05 13:10:09.057397\"},{\"uid\":\"12345\",\"Comment\":[],\"postid\":40,\"text\":\"I watch a lot of movies\",\"timestamp\":\"2017-10-05 13:10:09.071942\"},{\"uid\":\"12345\",\"Comment\":[],\"postid\":41,\"text\":\"I love to travel\",\"timestamp\":\"2017-10-05 13:10:09.090395\"},{\"uid\":\"23121\",\"Comment\":[],\"postid\":48,\"text\":\"Hi! This is Chavez!!\",\"timestamp\":\"2017-10-05 13:10:09.49956\"},{\"uid\":\"23121\",\"Comment\":[],\"postid\":49,\"text\":\"I live in Calcutta\",\"timestamp\":\"2017-10-05 13:10:09.54051\"},{\"uid\":\"23121\",\"Comment\":[],\"postid\":50,\"text\":\"I love to eat fish\",\"timestamp\":\"2017-10-05 13:10:09.65058\"},{\"uid\":\"23121\",\"Comment\":[],\"postid\":51,\"text\":\"Sunday is my favrate day\",\"timestamp\":\"2017-10-05 13:10:09.688596\"},{\"uid\":\"23121\",\"Comment\":[],\"postid\":52,\"text\":\"I play football daily\",\"timestamp\":\"2017-10-05 13:10:09.725009\"},{\"uid\":\"23121\",\"Comment\":[],\"postid\":53,\"text\":\"We have the hugli river in my city\",\"timestamp\":\"2017-10-05 13:10:09.753831\"},{\"uid\":\"23121\",\"Comment\":[],\"postid\":54,\"text\":\"Hi! This is Chavez!!\",\"timestamp\":\"2017-10-05 13:10:09.794189\"}],\"status\":true}");
            for (int i = 0; i < a.getData().length(); i++) {
                JSONObject o = a.getData().getJSONObject(i);
                posts.add(new Post(o));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /**/
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        postListView.setLayoutManager(layoutManager);
        final PostAdapter postAdapter = new PostAdapter(posts);
        postListView.setAdapter(postAdapter);

        // Network
        UniversalCookieJar persistentCookieJar = new UniversalCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));
        client = new OkHttpClient.Builder()
                .cookieJar(persistentCookieJar)
                .build();
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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.view_posts:
                break;
            case R.id.search:
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

    private class UserLogoutTask extends AsyncTask<Void, Void, ServerResponse> {
        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected ServerResponse doInBackground(Void... params) {
            try {
                Request request = new Request.Builder()
                        .url("http://192.168.0.5:8080/LogoutServlet")
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
                startActivity(new Intent(Home.this, Login.class));
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }

}
