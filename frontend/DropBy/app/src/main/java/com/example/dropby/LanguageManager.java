package com.example.dropby;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class LanguageManager {

    private static final String PREFS   = "app_settings";
    private static final String KEY     = "language";
    private static final String DEFAULT = "it";

    /** Restituisce il tag salvato ("it", "en-GB", …). */
    public static String get(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                  .getString(KEY, DEFAULT);
    }

    /** True se la lingua salvata è inglese. */
    public static boolean isEnglish(Context ctx) {
        return get(ctx).startsWith("en");
    }

    /** Salva il tag e applica il locale via AppCompatDelegate (gestisce ricreazione attività). */
    public static void saveAndApply(Context ctx, String languageTag) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
           .edit().putString(KEY, languageTag).apply();
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag));
    }

    /** Riapplica il locale salvato (da chiamare in Activity.onCreate). */
    public static void apply(Context ctx) {
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(get(ctx)));
    }
}
