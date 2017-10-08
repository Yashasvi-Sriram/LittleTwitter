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
import android.util.Log;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddPost extends AppCompatActivity {

    // TODO: 9/10/17
    // logout on invalid session
    // validate post string empty string etc...
    // try to fix picture size

    private EditText postContentView;
    private Button addPostButton;
    private Button addPhotoButton;
    private View progressView;
    private View addPostFormView;
    private ImageView addPostImage;
    private OkHttpClient client;

    private String imageBase64 = "";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        postContentView = (EditText) findViewById(R.id.add_post_data);
        addPostImage = (ImageView) findViewById(R.id.add_post_image);
        addPhotoButton = (Button) findViewById(R.id.add_post_image_button);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openDialogImage();
            }
        });
        addPostButton = (Button) findViewById(R.id.add_post_button);
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAddPost();
            }
        });
        progressView = findViewById(R.id.add_post_progress);
        addPostFormView = findViewById(R.id.add_post_form);

        // Network
        UniversalCookieJar persistentCookieJar = new UniversalCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));
        client = new OkHttpClient.Builder()
                .cookieJar(persistentCookieJar)
                .build();
    }

    private void openDialogImage() {

        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(AddPost.this);
        builder.setTitle("Add Photo");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
    }

    private void galleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE){
                imageBase64 = getBase64Image(data,1);
            }
            else if(requestCode == REQUEST_IMAGE_CAPTURE){
                Log.i("Entered Camera", "Camera entered");
                imageBase64 = getBase64Image(data,0);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private String getBase64Image(Intent data,int fromGallery){
        if(fromGallery==1){
            Bitmap bm=null;
            if (data != null) {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            addPostImage.setImageBitmap(bm);
            return convertBitmapToBase64(bm);
        }else{
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
            File destination = new File(Environment.getExternalStorageDirectory(),
                    System.currentTimeMillis() + ".jpg");
            FileOutputStream fo;
            try {
                destination.createNewFile();
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            addPostImage.setImageBitmap(thumbnail);
            return convertBitmapToBase64(thumbnail);
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    private void attemptAddPost(){
        String postContentText = postContentView.getText().toString();
        new AddPostTask(postContentText).execute();
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        addPostFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        addPostFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                addPostFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

    private class AddPostTask extends AsyncTask<Void, Void, ServerResponse>{

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
                        .add("content", postContentText)
                        .add("base64",imageBase64)
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
                } else {
                    Toast.makeText(AddPost.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }

}
