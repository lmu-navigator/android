package de.lmu.navigator.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.view.RealmAdapter;
import io.realm.RealmResults;

public abstract class BuildingsAdapter extends RealmAdapter<Building> {

    private OnBuildingClickedListener mClickListener;
    private TextDrawable.Builder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    public BuildingsAdapter(Context context, RealmResults<Building> items, boolean autoUpdate) {
        super(context, items, autoUpdate);
    }

    public void setOnBuildingClickListener(OnBuildingClickedListener listener) {
        mClickListener = listener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Building building = getItem(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onBuildingClicked(getItem(position));
                }
            }
        });
        onBindBilduing(holder, building);
    }

    protected abstract void onBindBilduing(RecyclerView.ViewHolder holder, Building building);

    public interface OnBuildingClickedListener {
        void onBuildingClicked(Building building);
    }

    public Drawable getPlaceholderDrawable(Building b, int size) {
        int color = mColorGenerator.getColor(b);
        return TextDrawable.builder()
                .beginConfig()
                    .textColor(Color.WHITE)
                    .toUpperCase()
                    .width(size)
                    .height(size)
                .endConfig()
                .buildRound(b.getDisplayName().substring(0, 1), color);
    }
}
