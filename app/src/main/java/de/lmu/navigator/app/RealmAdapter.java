package de.lmu.navigator.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

public abstract class RealmAdapter<T extends RealmObject> extends RecyclerView.Adapter {

    private RealmResults<T> mRealmResults;

    protected Context mContext;

    public RealmAdapter(Context context, RealmResults<T> realmResults,
                        boolean autoUpdate) {
        mContext = context;
        mRealmResults = realmResults;
        if (autoUpdate) {
            Realm.getInstance(context).addChangeListener(new RealmChangeListener() {
                @Override
                public void onChange() {
                    notifyDataSetChanged();
                }
            });
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
