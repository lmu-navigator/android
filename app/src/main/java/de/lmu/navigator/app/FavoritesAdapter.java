package de.lmu.navigator.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.lmu.navigator.R;
import de.lmu.navigator.database.model.Building;
import io.realm.RealmResults;

public class FavoritesAdapter extends RealmAdapter<Building> {

    private OnBuildingClickedListener mClickListener;

    public FavoritesAdapter(Context context, RealmResults<Building> realmResults,
                            boolean autoUpdate, OnBuildingClickedListener listener) {
        super(context, realmResults, autoUpdate);
        mClickListener = listener;
    }

    @Override
    public FavoritesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_favorite, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
        FavoritesAdapter.ViewHolder holder = (FavoritesAdapter.ViewHolder) vh;
        final Building building = getItem(position);
        holder.city.setText(building.getStreet().getCity().getName());
        holder.street.setText(building.getDisplayName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onBuildingClicked(building);
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.image)
        CircularImageView image;

        @InjectView(R.id.text_address1)
        TextView street;

        @InjectView(R.id.text_address2)
        TextView city;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.inject(this, v);
        }
    }

    public interface OnBuildingClickedListener {
        void onBuildingClicked(Building building);
    }
}
