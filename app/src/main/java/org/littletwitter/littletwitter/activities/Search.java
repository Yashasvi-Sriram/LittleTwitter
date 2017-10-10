package org.littletwitter.littletwitter.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;

import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.configuration.SharedPrefs;
import org.littletwitter.littletwitter.cookies.Keys;
import org.littletwitter.littletwitter.cookies.UniversalCookieJar;
import org.littletwitter.littletwitter.cookies.UniversalCookiePersistor;
import org.littletwitter.littletwitter.customadapters.SearchUserAutoCompleteAdapter;

import okhttp3.OkHttpClient;

public class Search extends AppCompatActivity implements SearchUserAutoCompleteAdapter.ActivityAPI {

    private String selectedUserId;
    private TextView uid;
    private TextView name;
    private TextView email;
    private View controlCenter;
    private AutoCompleteTextView searchBar;

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

        Button viewPosts = v.findViewById(R.id.view_posts);
        viewPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Search.this, UserPosts.class);
                i.putExtra("userId", selectedUserId);
                startActivity(i);
            }
        });
        Button toggleFollow = v.findViewById(R.id.toggle_follow);
        toggleFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        Button cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Search.this.finish();
            }
        });

        OkHttpClient client;
        UniversalCookieJar persistentCookieJar = new UniversalCookieJar(new SetCookieCache(), new UniversalCookiePersistor(this, SharedPrefs.SHARED_PREFS_NAME));
        client = new OkHttpClient.Builder()
                .cookieJar(persistentCookieJar)
                .build();

        searchBar.setAdapter(new SearchUserAutoCompleteAdapter(this, client));
    }

    @Override
    public void onSearchResultClick(String uid, String name, String email) {
        controlCenter.setVisibility(View.VISIBLE);
        selectedUserId = uid;
        this.uid.setText(uid);
        this.name.setText(name);
        this.email.setText(email);
        searchBar.dismissDropDown();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
    }

    @Override
    public void onInvalidSession() {
        getSharedPreferences(SharedPrefs.SHARED_PREFS_NAME, MODE_PRIVATE).edit().remove(Keys.JSESSIONID).apply();
        startActivity(new Intent(this, Login.class));
    }

}
