package de.lmu.navigator.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.model.BuildingPartOld;

public class BuildingPartAdapter extends BaseAdapter {

    private List<BuildingPartOld> mBuildingParts;

    private Context mContext;

    private int mCurrentSelection;

    public BuildingPartAdapter(Context context, List<BuildingPartOld> buildingParts) {
        mBuildingParts = buildingParts;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mBuildingParts.size();
    }

    @Override
    public Object getItem(int position) {
        return mBuildingParts.get(position);
    }

    public void setCurrentSelection(int position) {
        mCurrentSelection = position;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv;
        if (convertView == null) {
            tv = (TextView) LayoutInflater.from(mContext)
                    .inflate(R.layout.list_item_building_part, parent, false);
        } else {
            tv = (TextView) convertView;
        }

        tv.setText(mBuildingParts.get(position).getDisplayName());

        if (position == mCurrentSelection) {
            tv.setTextColor(mContext.getResources().getColor(R.color.green));
            tv.setTypeface(null, Typeface.BOLD);
        } else {
            tv.setTextColor(mContext.getResources().getColor(R.color.lightgrey));
            tv.setTypeface(null, Typeface.NORMAL);
        }

        return tv;
    }
}
