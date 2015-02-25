package de.lmu.navigator.app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import de.lmu.navigator.util.CheckableDrawerItem;

public class PrimaryDrawerAdapter extends BaseAdapter {

    private MainActivity mActivity;

    public PrimaryDrawerAdapter(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public int getCount() {
        return MainActivity.PrimaryDrawerItem.values().length;
    }

    @Override
    public MainActivity.PrimaryDrawerItem getItem(int position) {
        return MainActivity.PrimaryDrawerItem.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        CheckableDrawerItem v;
        if (convertView == null) {
            v = new CheckableDrawerItem(mActivity);
        } else {
            v = (CheckableDrawerItem) convertView;
        }

        final MainActivity.PrimaryDrawerItem item = getItem(position);
        v.setItem(item);

        return v;
    }
}
