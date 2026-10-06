package com.liskovsoft.smartyoutubetv2.tv.ui.dialogs.other;

public class LongClickListPreference extends androidx.preference.ListPreference {
    private final java.util.Map<String, Runnable> mLongClicks = new java.util.HashMap<>();

    public LongClickListPreference(android.content.Context context) {
        super(context);
    }

    public void setLongClicks(java.util.Map<String, Runnable> longClicks) {
        mLongClicks.clear();
        if (longClicks != null) {
            mLongClicks.putAll(longClicks);
        }
    }

    public Runnable getLongClick(String value) {
        return mLongClicks.get(value);
    }
}
