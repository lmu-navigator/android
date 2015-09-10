package de.lmu.navigator.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.lmu.navigator.R;
import de.lmu.navigator.database.ModelHelper;
import de.lmu.navigator.database.model.Building;
import io.realm.RealmResults;

public class FavoritesAdapter extends BuildingsAdapter {

    public FavoritesAdapter(Context context, RealmResults<Building> items, boolean autoUpdate) {
        super(context, items, autoUpdate);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_favorite, parent, false);
        return new ViewHolder(v);
    }

    @Override
    protected void onBindBilduing(RecyclerView.ViewHolder vh, Building building) {
        FavoritesAdapter.ViewHolder holder = (FavoritesAdapter.ViewHolder) vh;
        holder.name.setText(building.getDisplayName());
        Picasso.with(mContext)
               .load(ModelHelper.getThumbnailUrl(building))
               .resizeDimen(R.dimen.image_size_favorite, R.dimen.image_size_favorite)
               .centerCrop()
               .placeholder(R.drawable.lmu)
               .into(holder.image);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.image)
        CircularImageView image;

        @Bind(R.id.name)
        TextView name;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
