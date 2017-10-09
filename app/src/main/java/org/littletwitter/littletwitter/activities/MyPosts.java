package org.littletwitter.littletwitter.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;

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

public class MyPosts extends AppCompatActivity implements InfinitePostListFragment.ActivityAPI {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
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
        return URLSource.seeMyPosts();
    }

    @Override
    public List<Pair<String, String>> getURLParameters() {
        return new ArrayList<>();
    }

}
