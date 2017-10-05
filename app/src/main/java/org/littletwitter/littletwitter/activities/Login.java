package org.littletwitter.littletwitter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.responses.ServerResponse;
import org.littletwitter.littletwitter.responses.StringServerResponse;

import java.io.IOException;

public class Login extends AppCompatActivity {
    private EditText userIdView;
    private EditText passwordView;
    private Button logInButton;
    private View progressView;
    private View loginFormView;
    private OkHttpClient client;

    // TODO: 5/10/17 get and store cookies, extract url

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        client = new OkHttpClient();
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
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
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
                RequestBody requestBody = new FormEncodingBuilder()
                        .add("id", userId)
                        .add("password", password)
                        .build();
                Request request = new Request.Builder()
                        .url("http://192.168.0.5:8080/Login")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    return null;
                }

                String body = response.body().string();
                Log.i("hesoyam", body);

                return new StringServerResponse(body);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ServerResponse response) {
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }

}

