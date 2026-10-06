package com.liskovsoft.smartyoutubetv2.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.FormatItem;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.track.MediaTrack;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for audio language extraction and filtering logic in {@link AppDialogUtil}.
 */
public class AppDialogUtilAudioLanguageTest {

    private FormatItem createAudioFormat(String language) {
        return new FormatItem() {
            @Override public int getId() { return 0; }
            @Override public String getFormatId() { return ""; }
            @Override public CharSequence getTitle() { return ""; }
            @Override public boolean isDefault() { return false; }
            @Override public boolean isSelected() { return false; }
            @Override public boolean isPreset() { return false; }
            @Override public float getFrameRate() { return 0; }
            @Override public String getLanguage() { return language; }
            @Override public int getWidth() { return 0; }
            @Override public int getHeight() { return 0; }
            @Override public int getType() { return TYPE_AUDIO; }
            @Override public MediaTrack getTrack() { return null; }
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> invokeExtract(List<FormatItem> formats) throws Exception {
        Method method = AppDialogUtil.class.getDeclaredMethod("extractAvailableAudioLanguages", List.class);
        method.setAccessible(true);
        return (Map<String, String>) method.invoke(null, formats);
    }

    private boolean invokeContainsOriginal(List<FormatItem> formats) throws Exception {
        Method method = AppDialogUtil.class.getDeclaredMethod("containsOriginalAudioTrack", List.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, formats);
    }

    private boolean invokeIsSelected(String code, String currentLang) throws Exception {
        Method method = AppDialogUtil.class.getDeclaredMethod("isAudioLanguageSelected", String.class, String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, code, currentLang);
    }

    @Test
    public void extractsUniqueLanguagesAndIgnoresDuplicates() throws Exception {
        List<FormatItem> formats = Arrays.asList(
                createAudioFormat("en"),
                createAudioFormat("en"),
                createAudioFormat("es"),
                createAudioFormat("ja")
        );

        Map<String, String> languages = invokeExtract(formats);
        assertEquals(3, languages.size());
        assertTrue(languages.containsKey("en"));
        assertTrue(languages.containsKey("es"));
        assertTrue(languages.containsKey("ja"));
    }

    @Test
    public void cleansDubbedAndAutoMarkersFromLanguageTags() throws Exception {
        List<FormatItem> formats = Arrays.asList(
                createAudioFormat("es (dubbed)"),
                createAudioFormat("fr (auto)")
        );

        Map<String, String> languages = invokeExtract(formats);
        assertEquals(2, languages.size());
        assertTrue(languages.containsKey("es"));
        assertTrue(languages.containsKey("fr"));
    }

    @Test
    public void detectsOriginalAudioTrackVariants() throws Exception {
        assertTrue(invokeContainsOriginal(Arrays.asList(createAudioFormat(null))));
        assertTrue(invokeContainsOriginal(Arrays.asList(createAudioFormat(""))));
        assertTrue(invokeContainsOriginal(Arrays.asList(createAudioFormat("und"))));
        assertTrue(invokeContainsOriginal(Arrays.asList(createAudioFormat("original"))));

        assertFalse(invokeContainsOriginal(Arrays.asList(
                createAudioFormat("en"),
                createAudioFormat("es")
        )));
    }

    @Test
    public void matchesLanguageSelectionCorrectly() throws Exception {
        assertTrue(invokeIsSelected("es", "es"));
        assertTrue(invokeIsSelected("es-419", "es"));
        assertTrue(invokeIsSelected("es", "es-419"));
        assertFalse(invokeIsSelected("en", "es"));
        assertFalse(invokeIsSelected("", "es"));
        assertFalse(invokeIsSelected("es", ""));
    }

    @Test
    public void singleLanguageOrEmptyReturnsNullCategory() {
        // null or empty formats
        org.junit.Assert.assertNull(AppDialogUtil.createAudioLanguageCategory(null, null, null));
        org.junit.Assert.assertNull(AppDialogUtil.createAudioLanguageCategory(null, new ArrayList<>(), null));

        // only one language (e.g. only English)
        List<FormatItem> singleLang = Arrays.asList(
                createAudioFormat("en"),
                createAudioFormat("en")
        );
        org.junit.Assert.assertNull(AppDialogUtil.createAudioLanguageCategory(null, singleLang, null));

        // only original track
        List<FormatItem> onlyOriginal = Arrays.asList(
                createAudioFormat("original"),
                createAudioFormat(null)
        );
        org.junit.Assert.assertNull(AppDialogUtil.createAudioLanguageCategory(null, onlyOriginal, null));
    }
}
