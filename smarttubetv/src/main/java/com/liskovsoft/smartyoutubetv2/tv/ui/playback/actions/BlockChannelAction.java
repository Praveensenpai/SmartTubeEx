package com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import com.liskovsoft.smartyoutubetv2.tv.R;

/**
 * An action for blocking the currently playing channel.
 *
 * <p>Mirrors the grid long-press entry ({@code VideoMenuPresenter.appendBlockChannelButton}) so a
 * bad channel can be muted without leaving playback.
 */
public class BlockChannelAction extends PaddingAction {
    public BlockChannelAction(Context context) {
        super(R.id.action_block_channel);
        Drawable uncoloredDrawable = ContextCompat.getDrawable(context, R.drawable.action_block_channel);

        setIcon(uncoloredDrawable);
        setLabel1(context.getString(R.string.dialog_block_channel));
    }
}
