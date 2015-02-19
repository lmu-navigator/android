package de.lmu.navigator.util;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import de.lmu.navigator.R;
import de.lmu.navigator.app.MainActivity;

@EViewGroup(R.layout.drawer_item_primary)
public class CheckableDrawerItem extends FrameLayout implements Checkable {

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    @ViewById(R.id.drawer_item_icon)
    ImageView mIcon;

    @ViewById(R.id.drawer_item_title)
    TextView mTitle;

    private boolean mChecked = false;

    private MainActivity.PrimaryDrawerItem mItem;

    public CheckableDrawerItem(Context context) {
        super(context);
        setBackgroundResource(R.drawable.background_drawer_item);
    }

    public void setItem(MainActivity.PrimaryDrawerItem item) {
        mItem = item;
        mTitle.setText(mItem.titleResId);
        mIcon.setImageResource(mItem.iconResId);
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked != mChecked) {
            mChecked = checked;
            refreshDrawableState();
            mTitle.setTextColor(getResources().getColor(
                    mChecked ? R.color.green : R.color.drawer_item_text));
            mTitle.setTypeface(null, mChecked ? Typeface.BOLD : Typeface.NORMAL);
            mIcon.setImageResource(mChecked ? mItem.iconResIdChecked : mItem.iconResId);
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

}