package org.littletwitter.littletwitter.customadapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.activities.AddPost;
import org.littletwitter.littletwitter.configuration.URLSource;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by joshi on 9/10/17.
 */

public class SearchUserAutoCompleteAdapter extends BaseAdapter implements Filterable {


    private Context mContext;
    private OkHttpClient client;
    private JSONArray usersFromSearch = new JSONArray();

    public SearchUserAutoCompleteAdapter(Context context, OkHttpClient client) {
        this.mContext = context;
        this.client = client;
    }

    @Override
    public JSONObject getItem(int index) {
        try{
            return usersFromSearch.getJSONObject(index);
        }catch (org.json.JSONException e){
            return new JSONObject();
        }
    }

    @Override
    public int getCount() {
        return usersFromSearch.length();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.search_suggestions_layout, parent, false);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    
                }
            });
        }try {
            ((TextView) convertView.findViewById(R.id.search_uid)).setText(getItem(position).getString("uid"));
            ((TextView) convertView.findViewById(R.id.search_name)).setText(getItem(position).getString("name"));
            ((TextView) convertView.findViewById(R.id.search_email)).setText(getItem(position).getString("email"));
        }catch (Exception e ){

        }
        return convertView;
    }

    @Override
    public Filter getFilter(){
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null && constraint.length()>=3) {
                    JSONArray users = searchUsers(constraint.toString());
                    filterResults.values = users;
                    filterResults.count = users.length();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    usersFromSearch = (JSONArray) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    private JSONArray searchUsers(String searchQuery){
        JSONArray userList = null;
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(URLSource.search()).newBuilder()
                    .addQueryParameter("search",searchQuery);
            Request request = new Request.Builder()
                    .url(urlBuilder.build().toString())
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject responseObject = new JSONObject( response.body().string() );
            if(responseObject.getBoolean("status"))
                userList = responseObject.getJSONArray("data").getJSONArray(0);
        }catch (Exception e){
            e.printStackTrace();
        }
        return userList;
    }

}
