package org.littletwitter.littletwitter.activities;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.configuration.URLSource;
import org.littletwitter.littletwitter.responses.ServerResponse;
import org.littletwitter.littletwitter.responses.StringServerResponse;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Search extends AppCompatActivity {

    private EditText searchBar;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        View V = findViewById(R.id.activity_search);
        searchBar = V.findViewById(R.id.search_bar);

    }

    private void attemptSearchQuery(){
        String searchString = searchBar.getText().toString();
        new SearchQueryTask(searchString).execute();
    }

    private class SearchQueryTask extends AsyncTask<Void, Void, ServerResponse> {

        private final String searchString;

        private SearchQueryTask(String searchString) {
            this.searchString = searchString;
        }

//        @Override
//        protected void onPreExecute() {
//            showProgress(true);
//        }

        @Override
        protected ServerResponse doInBackground(Void... voids) {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("search", searchString)
                        .build();
                Request request = new Request.Builder()
                        .url(URLSource.search())
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
//            showProgress(false);
            if (response == null) {
                Toast.makeText(Search.this, "Server error", Toast.LENGTH_SHORT).show();
            } else {
                if (response.getStatus()) {
                    Toast.makeText(Search.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Search.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
//            showProgress(false);
        }
    }

}
