package de.lmu.navigator.search;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class SearchResultAdapter extends BaseAdapter {
    private Context mContext;
    private List<? extends Searchable> mItems;
    private String mCurrentQuery;

    public SearchResultAdapter(Context context, List<? extends Searchable> items) {
        mContext = context;
        mItems = items;
        mCurrentQuery = "";
    }

    public void setQueryResult(String query, List<Searchable> result) {
        mItems = result;
        mCurrentQuery = query;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Searchable getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchResultView view;
        if (convertView == null) {
            view = SearchResultView_.build(mContext);
        } else {
            view = (SearchResultView) convertView;
        }

        view.bind(getItem(position), mCurrentQuery);
        return view;
    }
}
