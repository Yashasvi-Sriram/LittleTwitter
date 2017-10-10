package org.littletwitter.littletwitter.customadapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.littletwitter.littletwitter.R;
import org.littletwitter.littletwitter.activities.Search;
import org.littletwitter.littletwitter.configuration.URLSource;
import org.littletwitter.littletwitter.responses.ArrayServerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchUserAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private OkHttpClient client;
    private List<SearchResult> searchResults = new ArrayList<>();

    public SearchUserAutoCompleteAdapter(Context context, OkHttpClient client) {
        this.context = context;
        this.client = client;
    }

    @Override
    public SearchResult getItem(int index) {
        return searchResults.get(index);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return searchResults.size();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final SearchResult searchResult = getItem(position);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.search_suggestions_layout, parent, false);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ActivityAPI) context).onSearchResultClick(searchResult.uid, searchResult.name, searchResult.email);
            }
        });
        ((TextView) view.findViewById(R.id.uid)).setText(searchResult.uid);
        ((TextView) view.findViewById(R.id.name)).setText(searchResult.name);
        ((TextView) view.findViewById(R.id.email)).setText(searchResult.email);
        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                // search only when search string >= 3
                if (charSequence != null && charSequence.length() >= 3) {
                    ArrayServerResponse response = searchUsers(charSequence.toString());
                    if (response == null) {
                        filterResults.count = 2;
                    } else {
                        filterResults.values = response;
                        filterResults.count = 1;
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                if (results != null && results.count > 0) {
                    if (results.count == 1) {
                        try {
                            ArrayServerResponse response = (ArrayServerResponse) results.values;
                            if (response.getStatus()) {
                                searchResults = SearchResult.fromJSONArray(response.getData());
                            } else {
                                Toast.makeText(context, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                if (response.getErrorMessage().equalsIgnoreCase("Invalid session")) {
                                    ((ActivityAPI) context).onInvalidSession();
                                }
                            }
                            notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            searchResults = new ArrayList<>();
                            notifyDataSetInvalidated();
                            Toast.makeText(context, "Client doesn't seem to know server well", Toast.LENGTH_SHORT).show();
                        }
                    } else if (results.count == 2) {
                        searchResults = new ArrayList<>();
                        Toast.makeText(context, "Server Error", Toast.LENGTH_SHORT).show();
                        notifyDataSetInvalidated();
                    }
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private ArrayServerResponse searchUsers(String searchQuery) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(URLSource.search()).newBuilder()
                    .addQueryParameter("search", searchQuery);
            Request request = new Request.Builder()
                    .url(urlBuilder.build().toString())
                    .build();

            Response response = client.newCall(request).execute();

            return new ArrayServerResponse(response.body().string());
        } catch (JSONException | IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class SearchResult {
        private String uid;
        private String email;
        private String name;

        private SearchResult(JSONObject obj) throws JSONException {
            uid = obj.getString("uid");
            name = obj.getString("name");
            email = obj.getString("email");
        }

        private static List<SearchResult> fromJSONArray(JSONArray jsonArray) throws JSONException {
            List<SearchResult> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(new SearchResult(jsonArray.getJSONObject(i)));
            }
            return list;
        }
    }

    public interface ActivityAPI {
        public void onSearchResultClick(String uid, String name, String email);

        public void onInvalidSession();
    }
}
