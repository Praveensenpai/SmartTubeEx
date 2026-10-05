package com.liskovsoft.smartyoutubetv2.common.prefs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.lang.reflect.Constructor;

/**
 * Regression test for the id-fallback branch of {@code BlockedChannelData.Channel.equals}.
 *
 * <p>Historically the id branch compared a {@code Channel} instance to a {@code String} id
 * ({@code Helpers.equals(channel, channel.channelId)}), which is always false. As a result a
 * blocked channel stored without a display name could never be matched by its id.
 *
 * <p>The nested {@code Channel} class is private and its {@code equals} only relies on pure-Java
 * helpers, so the test drives the compiled class through reflection. This keeps the test free of
 * Android/{@code AppPrefs} singletons.
 */
public class BlockedChannelDataChannelEqualsTest {
    private static final String CHANNEL_CLASS =
            "com.liskovsoft.smartyoutubetv2.common.prefs.BlockedChannelData$Channel";

    private Object newChannel(String id, String name) throws Exception {
        Class<?> clazz = Class.forName(CHANNEL_CLASS);
        Constructor<?> ctor = clazz.getDeclaredConstructor(String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(id, name);
    }

    @Test
    public void nameLessChannelsWithSameIdAreEqual() throws Exception {
        Object first = newChannel("UC123", null);
        Object second = newChannel("UC123", null);

        assertTrue("name-less channels sharing an id must be equal", first.equals(second));
    }

    @Test
    public void nameLessChannelsWithDifferentIdsAreNotEqual() throws Exception {
        Object first = newChannel("UC123", null);
        Object second = newChannel("UC999", null);

        assertFalse(first.equals(second));
    }

    @Test
    public void channelsWithEqualNamesAreEqual() throws Exception {
        Object first = newChannel("UC123", "Some Channel");
        Object second = newChannel("UC999", "Some Channel");

        assertTrue("channels with equal names must be equal", first.equals(second));
    }

    @Test
    public void channelsWithDifferentNamesAreNotEqual() throws Exception {
        Object first = newChannel("UC123", "Channel A");
        Object second = newChannel("UC123", "Channel B");

        assertFalse(first.equals(second));
    }
}
