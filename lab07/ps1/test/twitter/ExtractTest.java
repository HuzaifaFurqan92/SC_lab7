/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

/**
 * Testing strategy
 *
 * getTimespan(tweets):
 *   number of tweets: 1, 2, many
 *   order of timestamps: ascending, unordered
 *
 * getMentionedUsers(tweets):
 *   mentions: none, many (with different case), e-mail address (not a mention)
 */
public class ExtractTest {

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:30:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "carol", "third tweet, a bit later", d3);

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testGetTimespanOneTweet() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1));

        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d1, timespan.getEnd());
    }

    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));

        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }

    @Test
    public void testGetTimespanManyTweetsUnordered() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2, tweet3, tweet1));

        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d3, timespan.getEnd());
    }

    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1, tweet2));

        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersManyMentionsDifferentCase() {
        Tweet t1 = new Tweet(4, "alyssa", "hi @Carol and @dave", d1);
        Tweet t2 = new Tweet(5, "bbitdiddle", "again @CAROL", d2);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(t1, t2));

        // postcondition is underdetermined (case), so compare in lower case
        assertEquals("Carol and CAROL are the same user", 2, mentionedUsers.size());
        assertTrue(mentionedUsers.contains("carol"));
        assertTrue(mentionedUsers.contains("dave"));
    }

    @Test
    public void testGetMentionedUsersEmailIsNotMention() {
        Tweet t = new Tweet(6, "alyssa", "mail me at bitdiddle@mit.edu", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(t));

        assertTrue("e-mail addresses are not mentions", mentionedUsers.isEmpty());
    }
}
