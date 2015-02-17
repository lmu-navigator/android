package de.lmu.navigator.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.database.model.Building;

public class FavoritesAdapter extends BaseAdapter {

    private Context mContext;
    private List<Building> mBuildings;

    public FavoritesAdapter(Context context, List<Building> buildings) {
        mContext = context;
        mBuildings = buildings;
    }

    @Override
    public int getCount() {
        return mBuildings.size();
    }

    @Override
    public Building getItem(int position) {
        return mBuildings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item_favorite, parent, false);
        } else {
            view = convertView;
        }

        TextView address1 = (TextView) view.findViewById(R.id.text_address1);
        address1.setText(getItem(position).getDisplayName());

        TextView address2 = (TextView) view.findViewById(R.id.text_address2);
        address2.setText(getItem(position).getDisplayName());

        return view;
    }
}
