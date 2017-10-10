package org.littletwitter.littletwitter.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;

import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.configuration.SharedPrefs;
import org.littletwitter.littletwitter.configuration.URLSource;
import org.littletwitter.littletwitter.cookies.Keys;
import org.littletwitter.littletwitter.cookies.UniversalCookieJar;
import org.littletwitter.littletwitter.cookies.UniversalCookiePersistor;
import org.littletwitter.littletwitter.fragments.InfinitePostListFragment;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

public class UserPosts extends AppCompatActivity implements InfinitePostListFragment.ActivityAPI {

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            finish();
        }

        TextView userIdView = findViewById(R.id.activity_user_posts).findViewById(R.id.user_id);
        userIdView.setText(userId);
    }

    @Override
    public void onInvalidSession() {
        getSharedPreferences(SharedPrefs.SHARED_PREFS_NAME, MODE_PRIVATE).edit().remove(Keys.JSESSIONID).apply();
        startActivity(new Intent(this, Login.class));
    }

    @Override
    public OkHttpClient getClient() {
        UniversalCookieJar persistentCookieJar = new UniversalCookieJar(new SetCookieCache(), new UniversalCookiePersistor(this, SharedPrefs.SHARED_PREFS_NAME));
        return new OkHttpClient.Builder()
                .cookieJar(persistentCookieJar)
                .build();
    }

    @Override
    public String getCompleteURL() {
        return URLSource.seeUserPosts();
    }

    @Override
    public List<Pair<String, String>> getURLParameters() {
        List<Pair<String, String>> list = new ArrayList<>();
        list.add(new Pair<>("userId", userId));
        return list;
    }

}
