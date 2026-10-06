package com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers;

import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.BasePlayerController;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionCategory;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.FormatItem;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.TrackSelectorUtil;
import com.liskovsoft.smartyoutubetv2.common.utils.AppDialogUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HQDialogController extends BasePlayerController {
    private static final String TAG = HQDialogController.class.getSimpleName();
    private static final int VIDEO_FORMATS_ID = 132;
    private static final int AUDIO_FORMATS_ID = 133;
    // NOTE: using map, because same item could be changed time to time
    private final Map<Integer, OptionCategory> mCategories = new LinkedHashMap<>();
    private final Map<Integer, OptionCategory> mCategoriesInt = new LinkedHashMap<>();
    private final Set<Runnable> mHideListeners = new HashSet<>();
    private AppDialogPresenter mAppDialogPresenter;

    @Override
    public void onInit() {
        mAppDialogPresenter = AppDialogPresenter.instance(getContext());
    }

    @Override
    public void onViewResumed() {
        //updateBackgroundPlayback();
    }

    @Override
    public void onButtonClicked(int buttonId, int buttonState) {
        if (buttonId == R.id.lb_control_high_quality) {
            onHighQualityClicked();
        }
    }

    private void onHighQualityClicked() {
        fitVideoIntoDialog();

        addQualityCategories();
        addAudioLanguage();
        addPresetsCategory();
        addVideoZoomCategory();
        addNetworkEngine();
        addVideoBufferCategory();
        addAudioDelayCategory();
        addPitchEffectCategory();
        addSleepTimerCategory();
        //addBackgroundPlaybackCategory();

        appendOptions(mCategoriesInt);
        appendOptions(mCategories);

        mAppDialogPresenter.showDialog(getContext().getString(R.string.playback_settings), this::onDialogHide);
    }

    private void addQualityCategories() {
        if (getPlayer() == null) {
            return;
        }

        List<FormatItem> videoFormats = getPlayer().getVideoFormats();
        String videoFormatsTitle = getContext().getString(R.string.title_video_formats);

        List<FormatItem> audioFormats = getPlayer().getAudioFormats();
        String audioFormatsTitle = getContext().getString(R.string.title_audio_formats);

        addCategoryInt(OptionCategory.from(
                VIDEO_FORMATS_ID,
                OptionCategory.TYPE_RADIO_LIST,
                videoFormatsTitle,
                buildVideoFormatOptions(videoFormats)));
        addCategoryInt(OptionCategory.from(
                AUDIO_FORMATS_ID,
                OptionCategory.TYPE_RADIO_LIST,
                audioFormatsTitle,
                UiOptionItem.from(audioFormats, this::selectFormatOption, getContext().getString(R.string.option_disabled))));
    }

    /**
     * MOD: Group video formats by resolution. One row per resolution. The row applies the best
     * (or last chosen) codec for that resolution; long-press opens codec selection for that
     * resolution only. The row title carries an indicator of the currently selected codec.
     */
    private List<OptionItem> buildVideoFormatOptions(List<FormatItem> formats) {
        List<OptionItem> options = new ArrayList<>();

        if (formats == null) {
            return options;
        }

        Map<Integer, List<FormatItem>> groups = new LinkedHashMap<>();

        for (FormatItem format : formats) {
            if (format == null || format.getTrack() == null || format.getTrack().format == null) {
                continue;
            }

            int height = TrackSelectorUtil.getRealHeight(format.getTrack().format);

            if (height <= 0) {
                continue;
            }

            List<FormatItem> group = groups.get(height);

            if (group == null) {
                group = new ArrayList<>();
                groups.put(height, group);
            }

            group.add(format);
        }

        for (Map.Entry<Integer, List<FormatItem>> entry : groups.entrySet()) {
            List<FormatItem> group = entry.getValue();

            FormatItem selected = null;

            for (FormatItem format : group) {
                if (format.isSelected()) {
                    selected = format;
                    break;
                }
            }

            FormatItem best = selected != null ? selected : pickBestFormat(group);

            if (best == null || best.getTrack() == null || best.getTrack().format == null) {
                continue;
            }

            String label = TrackSelectorUtil.getResolutionLabel(best.getTrack().format);

            if (label == null) {
                label = entry.getKey() + "p";
            }

            String codec = selected != null ? TrackSelectorUtil.extractCodec(selected.getTrack().format) : null;
            CharSequence title = codec != null && !codec.isEmpty()
                    ? label + "  \u00B7  " + codec.toUpperCase(java.util.Locale.US)
                    : label;

            UiOptionItem option = (UiOptionItem) UiOptionItem.from(best, title, this::selectFormatOption, selected != null);

            if (option != null) {
                final List<FormatItem> codecGroup = group;
                final String codecLabel = label;
                option.setLongClick(() -> showCodecDialog(codecLabel, codecGroup));
            }

            options.add(option);
        }

        return options;
    }

    private FormatItem pickBestFormat(List<FormatItem> group) {
        FormatItem best = null;
        int bestWeight = -1;

        for (FormatItem format : group) {
            int weight = codecWeight(format);

            if (weight > bestWeight) {
                bestWeight = weight;
                best = format;
            }
        }

        return best != null ? best : (group.isEmpty() ? null : group.get(0));
    }

    private int codecWeight(FormatItem format) {
        if (format == null || format.getTrack() == null || format.getTrack().format == null) {
            return 0;
        }

        String codec = TrackSelectorUtil.extractCodec(format.getTrack().format);

        if (codec == null) {
            return 0;
        }

        switch (codec.toLowerCase(java.util.Locale.US)) {
            case "av1":
            case "av01":
                return 3;
            case "vp9":
            case "vp09":
                return 2;
            case "avc":
                return 1;
            default:
                return 0;
        }
    }

    /**
     * MOD: Codec chooser for a single resolution (opened on long-press).
     */
    private void showCodecDialog(String resolutionLabel, List<FormatItem> group) {
        List<OptionItem> codecOptions = new ArrayList<>();

        for (FormatItem format : group) {
            if (format == null || format.getTrack() == null || format.getTrack().format == null) {
                continue;
            }

            String codec = TrackSelectorUtil.extractCodec(format.getTrack().format);
            CharSequence title = codec != null && !codec.isEmpty()
                    ? codec.toUpperCase(java.util.Locale.US)
                    : format.getTitle();

            codecOptions.add(UiOptionItem.from(format, title, this::selectFormatOption, format.isSelected()));
        }

        mAppDialogPresenter.appendRadioCategory(resolutionLabel, codecOptions);
        mAppDialogPresenter.showDialog(resolutionLabel);
    }

    private void selectFormatOption(OptionItem option) {
        if (getPlayer() == null) {
            return;
        }

        FormatItem formatItem = UiOptionItem.toFormat(option);
        getPlayer().setFormat(formatItem);
        persistFormat(formatItem);

        if (getPlayerData().getFormat(formatItem.getType()).isPreset()) {
            // Preset currently active. Show warning about format reset.
            MessageHelpers.showMessage(getContext(), R.string.video_preset_enabled);
        }

        if (!getPlayer().containsMedia()) {
            getPlayer().reloadPlayback();
        }

        // Make result easily be spotted by the user
        if (formatItem.getType() == FormatItem.TYPE_VIDEO) {
            getPlayer().showOverlay(false);
        }
    }

    private void persistFormat(FormatItem formatItem) {
        if (formatItem.getType() == FormatItem.TYPE_VIDEO) {
            if (!getPlayerData().getFormat(FormatItem.TYPE_VIDEO).isPreset()) {
                getPlayerData().setFormat(formatItem);
            } else {
                getPlayerData().setTempVideoFormat(formatItem);
            }
        } else {
            getPlayerData().setFormat(formatItem);
        }
    }

    private void addVideoBufferCategory() {
        if (getPlayer() == null) {
            return;
        }
        addCategoryInt(AppDialogUtil.createVideoBufferCategory(getContext(),
                () -> getPlayer().restartEngine()));
    }

    private void addAudioDelayCategory() {
        if (getPlayer() == null) {
            return;
        }
        addCategoryInt(AppDialogUtil.createAudioDelayCategory(getContext(),
                () -> getPlayer().restartEngine()));
    }

    private void addPitchEffectCategory() {
        addCategoryInt(AppDialogUtil.createPitchEffectCategory(getContext()));
    }

    private void addSleepTimerCategory() {
        addCategoryInt(AppDialogUtil.createSleepTimerCategory(getContext()));
    }

    private void addAudioLanguage() {
        if (getPlayer() == null) {
            return;
        }
        OptionCategory category = AppDialogUtil.createAudioLanguageCategory(getContext(),
                getPlayer().getAudioFormats(),
                () -> getPlayer().restartEngine());
        if (category != null) {
            addCategoryInt(category);
        }
    }

    private void addNetworkEngine() {
        if (getPlayer() == null) {
            return;
        }
        addCategoryInt(AppDialogUtil.createNetworkEngineCategory(getContext(),
                () -> getPlayer().restartEngine()));
    }

    private void onDialogHide() {
        //updateBackgroundPlayback();

        for (Runnable listener : mHideListeners) {
            listener.run();
        }

        mHideListeners.clear();
        mCategories.clear();
        mCategoriesInt.clear();
    }

    //private void updateBackgroundPlayback() {
    //    ViewManager.instance(getContext()).blockTop(null);
    //
    //    if (getPlayer() != null) {
    //        getPlayer().setBackgroundMode(getPlayerData().getBackgroundMode());
    //    }
    //}

    //private void addBackgroundPlaybackCategory() {
    //    OptionCategory category =
    //            AppDialogUtil.createBackgroundPlaybackCategory(getContext(), getPlayerData(), GeneralData.instance(getContext()), this::updateBackgroundPlayback);
    //
    //    addCategoryInt(category);
    //}

    private void addPresetsCategory() {
        addCategoryInt(AppDialogUtil.createVideoPresetsCategory(
                getContext(), () -> {
                    if (getPlayer() == null) {
                        return;
                    }

                    FormatItem format = getPlayerData().getFormat(FormatItem.TYPE_VIDEO);
                    getPlayer().setFormat(format);

                    if (!getPlayer().containsMedia()) {
                        getPlayer().reloadPlayback();
                    }

                    // Make result easily be spotted by the user
                    getPlayer().showOverlay(false);
                }
        ));
    }

    private void addVideoZoomCategory() {
        if (getPlayer() == null) {
            return;
        }

        addCategoryInt(AppDialogUtil.createVideoZoomCategory(
                getContext(), () -> {
                    getPlayer().setResizeMode(getPlayerData().getResizeMode());
                    getPlayer().setZoomPercents(getPlayerData().getZoomPercents());

                    // Make result easily be spotted by the user
                    getPlayer().showOverlay(false);
                }));
    }

    private void removeCategoryInt(int id) {
        mCategoriesInt.remove(id);
    }

    private void addCategoryInt(OptionCategory category) {
        mCategoriesInt.put(category.id, category);
    }

    public void removeCategory(int id) {
        mCategories.remove(id);
    }

    public void addCategory(OptionCategory category) {
        mCategories.put(category.id, category);
    }

    public void addOnDialogHide(Runnable listener) {
        mHideListeners.add(listener);
    }

    public void removeOnDialogHide(Runnable listener) {
        mHideListeners.remove(listener);
    }

    private void appendOptions(Map<Integer, OptionCategory> categories) {
        for (OptionCategory category : categories.values()) {
            mAppDialogPresenter.appendCategory(category);
        }
    }
}
