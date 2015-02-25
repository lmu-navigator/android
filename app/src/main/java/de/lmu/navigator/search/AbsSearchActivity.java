package de.lmu.navigator.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.lmu.navigator.R;
import de.lmu.navigator.app.BaseActivity;
import de.lmu.navigator.view.DividerItemDecoration;
import retrofit.android.MainThreadExecutor;

public abstract class AbsSearchActivity extends BaseActivity
        implements TextWatcher, SearchResultAdapter.OnItemClickListener {
    // TODO: loading spinner

    private static final String LOG_TAG = AbsSearchActivity.class.getSimpleName();

    public static final String KEY_SEARCH_RESULT = "KEY_SEARCH_RESULT";

    @InjectView(R.id.recycler)
    RecyclerView mRecyclerView;

    @InjectView(android.R.id.empty)
    View mEmptyView;

    private ListeningExecutorService mBackgroundExecutor;
    private ListenableFuture<List<Searchable>> mSearchFuture;
    private Executor mUiExecutor;

    private List<? extends Searchable> mItems;
    private List<SearchScore> mScores;

    protected SearchResultAdapter mAdapter;
    private EditText mSearchViewText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.inject(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, R.drawable.divider));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //mRecyclerView.setEmptyView(mEmptyView);

        mBackgroundExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        mUiExecutor = new MainThreadExecutor();

        ListenableFuture loadFuture = mBackgroundExecutor.submit(new Runnable() {
            @Override
            public void run() {
                mItems = getItems();
                mScores = new ArrayList<SearchScore>(mItems.size());
                for (Searchable s : mItems)
                    mScores.add(new SearchScore(s));
            }
        });

        Futures.addCallback(loadFuture, new FutureCallback() {
            @Override
            public void onSuccess(Object result) {
                mAdapter = new SearchResultAdapter(AbsSearchActivity.this, mItems,
                        AbsSearchActivity.this);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Loading search items failed!" , t);
            }
        }, mUiExecutor);
    }

    public abstract List<? extends Searchable> getItems();

    public abstract int getSearchHintResId();

    @Override
    public void onItemClick(Searchable item) {
        Intent result = new Intent();
        result.putExtra(KEY_SEARCH_RESULT, item.getCode());
        setResult(RESULT_OK, result);

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        MenuItemCompat.expandActionView(searchItem);

        View actionView = MenuItemCompat.getActionView(searchItem);
        mSearchViewText = (EditText) actionView.findViewById(R.id.searchview_text);
        mSearchViewText.setHint(getSearchHintResId());

        setSearchTextListener();

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                finish();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void setSearchTextListener() {
        final InputMethodManager inputManager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mSearchViewText.addTextChangedListener(this);
        mSearchViewText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });

        mSearchViewText.requestFocus();
        inputManager.showSoftInput(mSearchViewText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void getSearchResults(final String query) {
        if (mSearchFuture != null) {
            mSearchFuture.cancel(true);
        }

        mSearchFuture = mBackgroundExecutor.submit(new Callable<List<Searchable>>() {
            @Override
            public List<Searchable> call() throws Exception {
                for (SearchScore s : mScores)
                    s.score(query);

                Collections.sort(mScores);

                ArrayList<Searchable> result = new ArrayList<Searchable>();
                for (SearchScore s : mScores) {
                    if (s.isMatch())
                        result.add(s.getObject());
                }

                return result;
            }
        });

        Futures.addCallback(mSearchFuture, new FutureCallback<List<Searchable>>() {
            @Override
            public void onSuccess(List<Searchable> result) {
                mAdapter.setQueryResult(query, result);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(LOG_TAG, "search request cancelled");
            }
        }, mUiExecutor);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // ignore
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // ignore
    }

    @Override
    public void afterTextChanged(Editable s) {
        getSearchResults(s.toString());
    }

    @Override
    protected void onDestroy() {
        mBackgroundExecutor.shutdownNow();
        super.onDestroy();
    }
}
