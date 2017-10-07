package org.littletwitter.littletwitter.customadapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.activities.AddComment;
import org.littletwitter.littletwitter.beans.Post;

import java.util.List;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

    private List<Post> posts;
    private Context context;

    public PostListAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
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
                Log.i("hesoyam", "add comment " + post);
                context.startActivity(new Intent(context, AddComment.class));
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
        private Button showAllComments;
        private Button addComment;
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
        }
    }
}