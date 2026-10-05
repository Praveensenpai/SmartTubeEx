package com.liskovsoft.smartyoutubetv2.common.app.models.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Regression test for {@code Video.belongsToFeedSection}.
 *
 * <p>Section-playlist ("use current section contents as a playlist") hijacked the "next"
 * video on algorithmic feed rows: opening a video from a Home shelf or from search results
 * played the next card in that same row instead of the real YouTube suggestion. The fix
 * marks feed rows and disables section-playlist for them.
 */
public class VideoFeedSectionTest {
    private Video videoInGroupOfType(int groupType) {
        Video video = new Video();
        video.videoId = "vid";

        VideoGroup group = VideoGroup.from(new ArrayList<>(Collections.singletonList(video)), null, -1);
        group.setType(groupType);

        return video;
    }

    @Test
    public void feedSectionsAreDetected() {
        int[] feedTypes = {
                MediaGroup.TYPE_HOME,
                MediaGroup.TYPE_SEARCH,
                MediaGroup.TYPE_RECOMMENDED,
                MediaGroup.TYPE_SUBSCRIPTIONS,
                MediaGroup.TYPE_MUSIC,
                MediaGroup.TYPE_NEWS,
                MediaGroup.TYPE_GAMING,
                MediaGroup.TYPE_TRENDING,
                MediaGroup.TYPE_SPORTS,
                MediaGroup.TYPE_MOVIES,
                MediaGroup.TYPE_LIVE,
                MediaGroup.TYPE_KIDS_HOME,
        };

        for (int type : feedTypes) {
            assertTrue("type " + type + " must be treated as a feed section",
                    videoInGroupOfType(type).belongsToFeedSection());
        }
    }

    @Test
    public void orderedSectionsAreNotFeedSections() {
        int[] orderedTypes = {
                MediaGroup.TYPE_CHANNEL_UPLOADS,
                MediaGroup.TYPE_CHANNEL,
                MediaGroup.TYPE_USER_PLAYLISTS,
                MediaGroup.TYPE_HISTORY,
        };

        for (int type : orderedTypes) {
            assertFalse("type " + type + " must keep section-playlist enabled",
                    videoInGroupOfType(type).belongsToFeedSection());
        }
    }

    @Test
    public void videoWithoutGroupIsNotFeedSection() {
        Video video = new Video();
        video.videoId = "vid";

        assertFalse(video.belongsToFeedSection());
    }
}
