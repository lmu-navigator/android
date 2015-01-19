package de.lmu.navigator.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.lmu.navigator.R;

public class SecondaryDrawerAdapter extends BaseAdapter {

    private MainActivity mActivity;

    public SecondaryDrawerAdapter(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public int getCount() {
        return MainActivity.SecondaryDrawerItem.values().length;
    }

    @Override
    public MainActivity.SecondaryDrawerItem getItem(int position) {
        return MainActivity.SecondaryDrawerItem.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = LayoutInflater.from(mActivity).inflate(R.layout.drawer_item_secondary, parent, false);
        } else {
            v = convertView;
        }

        final MainActivity.SecondaryDrawerItem item = getItem(position);

        TextView title = (TextView) v.findViewById(R.id.drawer_item_title);
        title.setText(item.titleResId);

        return v;
    }
}
