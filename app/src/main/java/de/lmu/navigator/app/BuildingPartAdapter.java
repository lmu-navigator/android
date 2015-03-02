package de.lmu.navigator.app;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.database.ModelHelper;
import de.lmu.navigator.database.model.BuildingPart;

public class BuildingPartAdapter extends RecyclerView.Adapter<BuildingPartAdapter.ViewHolder> {

    private int mCurrentSelection = 0;

    private Context mContext;

    private List<BuildingPart> mBuildingParts;

    private LayoutInflater mInflater;

    public BuildingPartAdapter(Context context, List<BuildingPart> buildingParts) {
        mContext = context;
        mBuildingParts = buildingParts;
        Collections.sort(mBuildingParts, ModelHelper.buildingPartComparator);
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.list_item_building_part, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final BuildingPart p = mBuildingParts.get(position);
        holder.name.setText(p.getName());

        if (position == mCurrentSelection) {
            holder.name.setTextColor(mContext.getResources().getColor(R.color.green));
            holder.name.setTypeface(null, Typeface.BOLD);
        } else {
            holder.name.setTextColor(mContext.getResources().getColor(R.color.lightgrey));
            holder.name.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentSelection = position;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBuildingParts.size();
    }

    public BuildingPart getSelectedBuildingPart() {
        return mBuildingParts.get(mCurrentSelection);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        public ViewHolder(View v) {
            super(v);
            name = (TextView) v;
        }
    }
}
