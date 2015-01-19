package de.lmu.navigator.search;

import android.content.Context;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.Locale;

import de.lmu.navigator.R;

@EViewGroup(R.layout.list_item_search_result)
public class SearchResultView extends LinearLayout {

    @ViewById(R.id.primary_text)
    TextView mPrimaryText;

    @ViewById(R.id.secondary_text)
    TextView mSecondaryText;

    public SearchResultView(Context context) {
        super(context);
    }

    public void bind(final Searchable searchable, String query) {
        setTextWithColor(mPrimaryText, searchable.getPrimaryText(), query,
                getResources().getColor(R.color.green));
        setTextWithColor(mSecondaryText, searchable.getSecondaryText(), query,
                getResources().getColor(R.color.green));
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
}
