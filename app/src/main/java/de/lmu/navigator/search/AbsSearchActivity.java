package de.lmu.navigator.search;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.app.BaseActivity;

@EActivity
public abstract class AbsSearchActivity extends BaseActivity implements TextWatcher {

    public static final String KEY_SEARCH_RESULT = "result";

    @SystemService
    InputMethodManager mInputManager;

    @ViewById(android.R.id.list)
    ListView mListView;

    @ViewById(android.R.id.empty)
    View mEmptyView;

    private List<? extends Searchable> mItems;
    private List<SearchScore> mScores;

    protected SearchResultAdapter mAdapter;
    private EditText mSearchViewText;

    @AfterViews
    void init() {
        // TODO: show spinner while loading
        mListView.setEmptyView(mEmptyView);
        loadItems();
    }

    private void setSearchTextListener() {
        mSearchViewText.addTextChangedListener(this);
        mSearchViewText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mInputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });

        mSearchViewText.requestFocus();
        //mSearchViewText.performClick();
        mInputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    @Background
    void loadItems() {
        mItems = getItems();
        mScores = new ArrayList<SearchScore>(mItems.size());
        for (Searchable s : mItems)
            mScores.add(new SearchScore(s));

        initAdapter();
    }

    public abstract List<? extends Searchable> getItems();

    public abstract int getSearchHintResId();

    @UiThread
    void initAdapter() {
        mAdapter = new SearchResultAdapter(this, mItems);
        mListView.setAdapter(mAdapter);
    }

    @Background
    void getSearchResults(String query) {
        // TODO: cancel older requests
        for (SearchScore s : mScores)
            s.score(query);

        Collections.sort(mScores);

        ArrayList<Searchable> result = new ArrayList<Searchable>();
        for (SearchScore s : mScores) {
            if (s.isMatch())
                result.add(s.getObject());
        }

        showResults(result, query);
    }

    @UiThread
    void showResults(List<Searchable> result, String query) {
        mAdapter.setQueryResult(query, result);
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

    @ItemClick(android.R.id.list)
    protected void onListItemClick(int position) {
        Intent result = new Intent();
        result.putExtra(KEY_SEARCH_RESULT, mAdapter.getItem(position).getCode());
        setResult(RESULT_OK, result);

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        //MenuItemCompat.setActionView(searchItem, R.layout.actionview_search);
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
}
