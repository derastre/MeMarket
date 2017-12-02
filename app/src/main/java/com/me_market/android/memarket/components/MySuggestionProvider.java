package com.me_market.android.memarket.components;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by aederas on 02/12/2017.
 */

public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.me_market.android.memarket.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
