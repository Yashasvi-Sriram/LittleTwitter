package org.littletwitter.littletwitter.customadapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.activities.Home;
import org.littletwitter.littletwitter.activities.Login;
import org.littletwitter.littletwitter.beans.Comment;
import org.littletwitter.littletwitter.beans.Post;
import org.littletwitter.littletwitter.configuration.URLSource;
import org.littletwitter.littletwitter.cookies.Keys;
import org.littletwitter.littletwitter.responses.ArrayServerResponse;
import org.littletwitter.littletwitter.responses.ServerResponse;
import org.littletwitter.littletwitter.responses.StringServerResponse;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

    private List<Post> posts;
    private Context context;
    private OkHttpClient client;

    public PostListAdapter(List<Post> posts, Context context, OkHttpClient client) {
        this.posts = posts;
        this.context = context;
        this.client = client;
    }

    public void add(int position, Post item) {
        posts.add(position, item);
        notifyItemInserted(position);
    }

    public void add(Post item) {
        posts.add(item);
        notifyItemInserted(posts.size());
    }

    public void remove(int position) {
        posts.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public PostListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View v = layoutInflater.inflate(R.layout.post_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Post post = posts.get(holder.getAdapterPosition());
        holder.userId.setText(post.getUid());
        holder.text.setText(post.getText());
        holder.timestamp.setText(post.getTimestamp());

        final CommentListAdapter commentListAdapter = new CommentListAdapter(post.getComments());
        holder.commentsListView.setAdapter(commentListAdapter);
        if (post.getComments().size() <= Config.DEFAULT_NO_OF_COMMENT_TO_DISPLAY) {
            holder.showAllComments.setVisibility(View.GONE);
        } else {
            holder.showAllComments.setVisibility(View.VISIBLE);
        }

        holder.showAllComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentListAdapter.showAll();
                holder.showAllComments.setVisibility(View.GONE);
            }
        });

        holder.addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComment(holder.getAdapterPosition(), post.getPostId(), holder.newCommentText.getText().toString());
                holder.newCommentText.setText("");
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView userId;
        private TextView text;
        private TextView timestamp;
        private RecyclerView commentsListView;
        private ImageButton showAllComments;
        private ImageButton addComment;
        private EditText newCommentText;
        private View layout;

        private ViewHolder(View v) {
            super(v);
            layout = v;
            userId = v.findViewById(R.id.user_id);
            text = v.findViewById(R.id.text);
            timestamp = v.findViewById(R.id.timestamp);
            commentsListView = v.findViewById(R.id.comments_list_view);
            LinearLayoutManager postListViewLayoutManager = new LinearLayoutManager(context);
            commentsListView.setLayoutManager(postListViewLayoutManager);

            showAllComments = v.findViewById(R.id.show_all_comments);
            addComment = v.findViewById(R.id.add_comment);
            newCommentText = v.findViewById(R.id.new_comment_text);
        }
    }

    private void addComment(int postPosition, int postId, String text) {
        if (text.equals("")) {
            return;
        }
        new AddCommentTask(postPosition, postId, text).execute();
    }

    private class AddCommentTask extends AsyncTask<Void, Void, ServerResponse> {
        private int postPosition;
        private int postId;
        private String text;

        public AddCommentTask(int postPosition, int postId, String text) {
            this.postPosition = postPosition;
            this.postId = postId;
            this.text = text;
        }

        @Override
        protected ServerResponse doInBackground(Void... params) {
            try {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URLSource.addComment()).newBuilder()
                        .addQueryParameter("postId", String.valueOf(postId))
                        .addQueryParameter("text", String.valueOf(text));

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
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
            if (response == null) {
                Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
            } else {
                if (!response.getStatus()) {
                    Toast.makeText(context, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    if (response.getErrorMessage().equalsIgnoreCase("Invalid session")) {
                        context.getSharedPreferences(Keys.JSESSIONID, Context.MODE_PRIVATE).edit().remove(Keys.JSESSIONID).apply();
                        context.startActivity(new Intent(context, Login.class));
                    }
                } else {
                    StringServerResponse ssr = (StringServerResponse) response;
                    Toast.makeText(context, ssr.getData(), Toast.LENGTH_SHORT).show();
                    posts.get(postPosition).getComments().add(new Comment("you", "you", text, "now"));
                    notifyItemChanged(postPosition);
                }
            }
        }
    }

}