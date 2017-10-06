package org.littletwitter.littletwitter.customadapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.beans.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> posts;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
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
    public PostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View v = layoutInflater.inflate(R.layout.post_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Post post = posts.get(holder.getAdapterPosition());
        holder.userId.setText(post.getUid());
        holder.userId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("hesoyam", "on click");
            }
        });

        holder.text.setText(post.getText());
        holder.timestamp.setText(post.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView userId;
        private TextView text;
        private TextView timestamp;
        private View layout;

        private ViewHolder(View v) {
            super(v);
            layout = v;
            userId = v.findViewById(R.id.user_id);
            text = v.findViewById(R.id.text);
            timestamp = v.findViewById(R.id.timestamp);
        }
    }
}