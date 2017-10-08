package org.littletwitter.littletwitter.customadapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.beans.Comment;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {

    private List<Comment> comments;
    private boolean showAll = false;

    public CommentListAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void add(int position, Comment item) {
        comments.add(position, item);
        notifyItemInserted(position);
    }

    public void add(Comment item) {
        comments.add(item);
        notifyItemInserted(comments.size());
    }

    public void remove(int position) {
        comments.remove(position);
        notifyItemRemoved(position);
    }

    public void showAll() {
        showAll = true;
        notifyItemRangeChanged(Config.DEFAULT_NO_OF_COMMENT_TO_DISPLAY, comments.size());
    }

    @Override
    public CommentListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View v = layoutInflater.inflate(R.layout.comment_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Comment post = comments.get(holder.getAdapterPosition());
        holder.name.setText(post.getName());
        holder.text.setText(post.getText());
        holder.timestamp.setText(post.getTimestamp());
        if (position >= Config.DEFAULT_NO_OF_COMMENT_TO_DISPLAY && !showAll) {
            holder.container.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView text;
        private TextView timestamp;
        private View layout;
        private View container;

        private ViewHolder(View v) {
            super(v);
            layout = v;
            name = v.findViewById(R.id.name);
            text = v.findViewById(R.id.text);
            timestamp = v.findViewById(R.id.timestamp);
            container = v.findViewById(R.id.container);
        }
    }
}