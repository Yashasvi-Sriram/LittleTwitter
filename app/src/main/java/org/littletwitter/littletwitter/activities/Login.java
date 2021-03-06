package org.littletwitter.littletwitter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.json.JSONException;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.configuration.SharedPrefs;
import org.littletwitter.littletwitter.configuration.URLSource;
import org.littletwitter.littletwitter.cookies.Keys;
import org.littletwitter.littletwitter.cookies.UniversalCookieJar;
import org.littletwitter.littletwitter.cookies.UniversalCookiePersistor;
import org.littletwitter.littletwitter.customadapters.Config;
import org.littletwitter.littletwitter.responses.ServerResponse;
import org.littletwitter.littletwitter.responses.StringServerResponse;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {
    private EditText userIdView;
    private EditText passwordView;
    private Button logInButton;
    private View progressView;
    private View loginFormView;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // If you have a session_id cookie then directly go to Home
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefs.SHARED_PREFS_NAME, MODE_PRIVATE);
        if (sharedPreferences.getString(Keys.JSESSIONID, null) != null) {
            startActivity(new Intent(this, Home.class));
        }

        // UI
        userIdView = (EditText) findViewById(R.id.user_id);
        passwordView = (EditText) findViewById(R.id.password);
        logInButton = (Button) findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);

        // Network
        UniversalCookieJar persistentCookieJar = new UniversalCookieJar(new SetCookieCache(), new UniversalCookiePersistor(this, SharedPrefs.SHARED_PREFS_NAME));
        client = new OkHttpClient.Builder()
                .cookieJar(persistentCookieJar)
                .build();
    }

    private void attemptLogin() {
        String userId = userIdView.getText().toString();
        String password = passwordView.getText().toString();
        new UserLoginTask(userId, password).execute();
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

    private class UserLoginTask extends AsyncTask<Void, Void, ServerResponse> {
        private final String userId;
        private final String password;

        private UserLoginTask(String userId, String password) {
            this.userId = userId;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected ServerResponse doInBackground(Void... params) {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("userId", URLEncoder.encode(userId, "utf-8"))
                        .add("password", URLEncoder.encode(password, "utf-8"))
                        .build();
                Request request = new Request.Builder()
                        .url(URLSource.login())
                        .post(requestBody)
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
                Toast.makeText(Login.this, "Server error", Toast.LENGTH_SHORT).show();
            } else {
                if (response.getStatus()) {
                    startActivity(new Intent(Login.this, Home.class));
                } else {
                    Toast.makeText(Login.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }

}

