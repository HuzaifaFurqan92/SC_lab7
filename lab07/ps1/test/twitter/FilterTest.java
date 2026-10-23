/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Testing strategy
 *
 * writtenBy(tweets, username):
 *   matches: one, several (order kept); username case: same, different
 *
 * inTimespan(tweets, timespan):
 *   tweets: inside / outside the timespan; exactly at the start or end (inclusive)
 *
 * containing(tweets, words):
 *   matches: none, several; case: same, different; many words (any one matches)
 */
public class FilterTest {

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");
    private static final Instant d4 = Instant.parse("2016-02-17T13:00:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "alyssa", "Talk about Rivest again tomorrow", d3);
    private static final Tweet tweet4 = new Tweet(4, "carol", "lunch at noon", d4);

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testWrittenByOneMatch() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "bbitdiddle");

        assertEquals("expected singleton list", Arrays.asList(tweet2), writtenBy);
    }

    @Test
    public void testWrittenByManyMatchesDifferentCase() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2, tweet3), "ALYSSA");

        assertEquals("expected both tweets in order", Arrays.asList(tweet1, tweet3), writtenBy);
    }

    @Test
    public void testInTimespanMultipleTweetsMultipleResults() {
        Timespan span = new Timespan(Instant.parse("2016-02-17T09:00:00Z"), d3);
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2, tweet4), span);

        assertEquals("expected tweets 1 and 2 in order", Arrays.asList(tweet1, tweet2), inTimespan);
    }

    @Test
    public void testInTimespanBoundariesAreInclusive() {
        Timespan span = new Timespan(d2, d3); // tweet2 at start, tweet3 at end
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2, tweet3, tweet4), span);

        assertEquals("tweets at start and end are inside", Arrays.asList(tweet2, tweet3), inTimespan);
    }

    @Test
    public void testContainingNoMatch() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("obama"));

        assertTrue("expected empty list", containing.isEmpty());
    }

    @Test
    public void testContainingManyMatchesDifferentCase() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2, tweet3, tweet4), Arrays.asList("RIVEST"));

        assertEquals("case-insensitive, input order kept", Arrays.asList(tweet1, tweet2, tweet3), containing);
    }

    @Test
    public void testContainingManyWordsMatchesAny() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2, tweet4), Arrays.asList("lunch", "minutes"));

        assertEquals("at least one of the words", Arrays.asList(tweet2, tweet4), containing);
    }
}
