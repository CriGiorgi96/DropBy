package com.example.dropby;

import android.content.Context;

/**
 * Reads the "language" preference saved by StartFragment / HomeActivity
 * ("it" = Italian, "en-GB" = English) and provides helpers to pick the
 * right field from bilingual API responses.
 */
public class LangUtil {

    private static final String PREFS = "app_settings";
    private static final String KEY   = "language";

    public static boolean isEnglish(Context ctx) {
        String lang = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, "it");
        return lang.startsWith("en");
    }

    /**
     * Returns the preferred string based on the saved locale, falling back to
     * the other language if the preferred one is null/empty.
     */
    public static String pick(Context ctx, String italian, String english) {
        if (isEnglish(ctx)) {
            return (english != null && !english.isEmpty()) ? english : italian;
        }
        return (italian != null && !italian.isEmpty()) ? italian : english;
    }
}
