package de.lmu.navigator.search;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.lmu.navigator.R;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private Context mContext;
    private List<? extends Searchable> mItems = new ArrayList<>();
    private String mCurrentQuery = "";
    private OnItemClickListener mClickListener;

    public SearchResultAdapter(Context context, OnItemClickListener listener) {
        mContext = context;
        mClickListener = listener;
    }

    public void setQueryResult(String query, List<? extends Searchable> result) {
        mItems = result;
        mCurrentQuery = query;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_search_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Searchable item = getItem(position);
        setTextWithColor(holder.primaryText, item.getPrimaryText(), mCurrentQuery,
                mContext.getResources().getColor(R.color.green));
        setTextWithColor(holder.secondaryText, item.getSecondaryText(), mCurrentQuery,
                mContext.getResources().getColor(R.color.green));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Searchable getItem(int position) {
        return mItems.get(position);
    }

    private void setTextWithColor(TextView view, String fulltext,
                                  String subtext, int color) {
        // assign text to text view
        view.setText(fulltext, TextView.BufferType.SPANNABLE);

        if (subtext.length() == 0)
            return;

        // search for the substring
        int i = fulltext.toLowerCase(Locale.GERMAN).indexOf(
                subtext.toLowerCase(Locale.GERMAN));
        if (i == -1)
            return;

        // substring was found, set the color
        Spannable str = (Spannable) view.getText();
        str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.primary_text)
        TextView primaryText;

        @BindView(R.id.secondary_text)
        TextView secondaryText;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }

    public interface OnItemClickListener {
        void onItemClick(Searchable item);
    }
}
