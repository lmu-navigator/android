package de.lmu.navigator.view;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

public abstract class RealmAdapter<T extends RealmObject> extends RecyclerView.Adapter {

    private RealmResults<T> mRealmResults;

    protected Context mContext;

    protected LayoutInflater mInflater;

    private RealmChangeListener<Realm> mChangeListener = new RealmChangeListener<Realm>() {
        @Override
        public void onChange(Realm realm) {
            notifyDataSetChanged();
        }
    };

    public RealmAdapter(Context context, RealmResults<T> realmResults,
                        boolean autoUpdate) {
        mContext = context;
        mRealmResults = realmResults;
        mInflater = LayoutInflater.from(mContext);

        if (autoUpdate) {
            Realm.getDefaultInstance().addChangeListener(mChangeListener);
        }
    }

    @Override
    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    public int getItemCount() {
        return mRealmResults.size();
    }

    public T getItem(int position) {
        return mRealmResults.get(position);
    }
}
