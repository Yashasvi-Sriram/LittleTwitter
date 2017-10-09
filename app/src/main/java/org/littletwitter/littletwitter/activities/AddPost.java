package org.littletwitter.littletwitter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.json.JSONException;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.configuration.URLSource;
import org.littletwitter.littletwitter.cookies.UniversalCookieJar;
import org.littletwitter.littletwitter.responses.ServerResponse;
import org.littletwitter.littletwitter.responses.StringServerResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddPost extends AppCompatActivity {

    private final int REQUEST_IMAGE_CAPTURE_CODE = 1;
    private final int SELECT_IMAGE_CODE = 2;

    private EditText text;
    private View progressBar;
    private View form;
    private ImageView image;
    private OkHttpClient client;

    private String base64Image = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        // UI
        View v = findViewById(R.id.activity_add_post);
        text = v.findViewById(R.id.text);
        image = v.findViewById(R.id.image);
        Button selectImageBtn = v.findViewById(R.id.select_image_btn);
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
        progressBar = v.findViewById(R.id.progress_bar);
        form = v.findViewById(R.id.form);

        // Network
        UniversalCookieJar persistentCookieJar = new UniversalCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));
        client = new OkHttpClient.Builder()
                .cookieJar(persistentCookieJar)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.submit:
                attemptAddPost();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECT_IMAGE_CODE) {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    image.setImageBitmap(bitmap);
                    base64Image = convertBitmapToBase64(bitmap);
                } else if (requestCode == REQUEST_IMAGE_CAPTURE_CODE) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                    File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
                    FileOutputStream fo;
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    image.setImageBitmap(bitmap);
                    base64Image = convertBitmapToBase64(bitmap);
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, "Some problem occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDialog() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add an Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
//                        cameraIntent();
                        Toast.makeText(AddPost.this, "Sorry, this feature is not yet available", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        galleryIntent();
                        break;
                    case 2:
                        dialog.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE_CODE);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_IMAGE_CODE);
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void attemptAddPost() {
        String postText = text.getText().toString();
        if (postText.equals("") && base64Image.equals("")) {
            Toast.makeText(this, "Empty post!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (base64Image.length() > 1000000) {
            Toast.makeText(this, "Image too big to post!", Toast.LENGTH_SHORT).show();
            return;
        }
        new AddPostTask(postText).execute();
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        form.setVisibility(show ? View.GONE : View.VISIBLE);
        form.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                form.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private class AddPostTask extends AsyncTask<Void, Void, ServerResponse> {

        private final String postContentText;

        private AddPostTask(String postContentText) {
            this.postContentText = postContentText;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected ServerResponse doInBackground(Void... voids) {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("text", postContentText)
                        .add("base64Image", base64Image)
                        .build();
                Request request = new Request.Builder()
                        .url(URLSource.addPost())
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
                Toast.makeText(AddPost.this, "Server error", Toast.LENGTH_SHORT).show();
            } else {
                if (response.getStatus()) {
                    Toast.makeText(AddPost.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddPost.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    if (response.getErrorMessage().equalsIgnoreCase("Invalid session")) {
                        startActivity(new Intent(AddPost.this, Login.class));
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }
}
