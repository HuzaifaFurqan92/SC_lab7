/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Testing strategy
 *
 * The postcondition of guessFollowsGraph() is underdetermined: the spec only
 * demands that "Ernie follows Bert" is in the graph when Ernie @-mentions Bert,
 * and it allows extra evidence. So these tests only check that the required
 * edges ARE present (and, where no evidence exists at all, that no one follows
 * anyone). They never check that the graph has exactly some size, and they
 * compare usernames in lower case because usernames are case-insensitive.
 * The spec also says that a username appears at most once as a key and in any
 * follows set, which the upper-case tests check. Tests of a stronger spec would
 * go into a separate class.
 *
 * guessFollowsGraph(tweets):
 *   list of tweets: empty, 1, many
 *   mentions per tweet: none, one, many
 *   tweets per author: one, many (mentions of the author's tweets are merged)
 *   same mention repeated / written in a different case: user appears once
 *   mention of oneself: not a follow (users can't follow themselves)
 *   e-mail address in the text: not a mention
 *
 * influencers(followsGraph):
 *   graph: empty, one user without followers, one influencer, several users
 *   follower counts: all different (strict order), tied
 *   usernames in different case: the same person, counted once
 *
 * Only the order of users with different follower counts is checked; the order
 * among tied users is not specified by the spec and is never tested.
 */
public class SocialNetworkTest {

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:30:00Z");

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // ---------- helpers (they keep the tests independent of the case and of
    // the exact set of keys that an implementation chooses) ----------

    /** @return the graph with all usernames in lower case */
    private static Map<String, Set<String>> lower(Map<String, Set<String>> graph) {
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            Set<String> follows =
                    result.computeIfAbsent(entry.getKey().toLowerCase(), k -> new HashSet<>());
            for (String name : entry.getValue()) {
                follows.add(name.toLowerCase());
            }
        }
        return result;
    }

    /** The spec: a username appears at most once as a key and in any map[A] set. */
    private static void assertEachUserOnce(Map<String, Set<String>> graph) {
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            assertTrue("key " + entry.getKey() + " appears twice (different case)",
                    keys.add(entry.getKey().toLowerCase()));
            Set<String> names = new HashSet<>();
            for (String name : entry.getValue()) {
                assertTrue("follows set has " + name + " twice (different case)",
                        names.add(name.toLowerCase()));
            }
        }
    }

    /** @return the people that 'user' follows in the graph, empty if none */
    private static Set<String> follows(Map<String, Set<String>> graph, String user) {
        Set<String> set = lower(graph).get(user);
        return set == null ? new HashSet<String>() : set;
    }

    /** @return the list of usernames in lower case */
    private static List<String> lower(List<String> names) {
        List<String> result = new ArrayList<>();
        for (String name : names) {
            result.add(name.toLowerCase());
        }
        return result;
    }

    private static Map<String, Set<String>> graphOf(Object... pairs) {
        // pairs: "ernie", new String[]{"bert", "oscar"}, "bert", new String[]{...}
        Map<String, Set<String>> graph = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            graph.put((String) pairs[i], new HashSet<>(Arrays.asList((String[]) pairs[i + 1])));
        }
        return graph;
    }

    // ---------- guessFollowsGraph() ----------

    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> followsGraph =
                SocialNetwork.guessFollowsGraph(new ArrayList<Tweet>());

        assertTrue("expected empty graph", followsGraph.isEmpty());
    }

    @Test
    public void testGuessFollowsGraphNoMentions() {
        Tweet t1 = new Tweet(1, "ernie", "is it reasonable to talk about rivest so much?", d1);
        Tweet t2 = new Tweet(2, "bert", "rivest talk in 30 minutes", d2);
        Map<String, Set<String>> followsGraph =
                SocialNetwork.guessFollowsGraph(Arrays.asList(t1, t2));

        // an implementation may or may not list the authors as keys, but nobody follows anybody
        for (Set<String> follows : followsGraph.values()) {
            assertTrue("no mention, so no evidence of any follow", follows.isEmpty());
        }
    }

    @Test
    public void testGuessFollowsGraphEmailIsNoEvidence() {
        Tweet t = new Tweet(1, "ernie", "write to oscar@mit.edu for details", d1);
        Map<String, Set<String>> followsGraph =
                SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        Set<String> follows = follows(followsGraph, "ernie");
        assertFalse("an e-mail address is not a mention", follows.contains("mit"));
        assertFalse("an e-mail address is not a mention", follows.contains("oscar"));
    }

    @Test
    public void testGuessFollowsGraphSingleMention() {
        Tweet t = new Tweet(1, "ernie", "hello @bert", d1);
        Map<String, Set<String>> followsGraph =
                SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        assertTrue("ernie must be in the graph", lower(followsGraph).containsKey("ernie"));
        assertTrue("ernie follows bert", follows(followsGraph, "ernie").contains("bert"));
    }

    @Test
    public void testGuessFollowsGraphMultipleMentions() {
        Tweet t = new Tweet(1, "ernie", "thanks @bert and @oscar, see @grover", d1);
        Map<String, Set<String>> followsGraph =
                SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        Set<String> follows = follows(followsGraph, "ernie");
        assertTrue("ernie follows bert", follows.contains("bert"));
        assertTrue("ernie follows oscar", follows.contains("oscar"));
        assertTrue("ernie follows grover", follows.contains("grover"));
    }

    @Test
    public void testGuessFollowsGraphMultipleTweetsFromOneUser() {
        Tweet t1 = new Tweet(1, "ernie", "hello @bert", d1);
        Tweet t2 = new Tweet(2, "ernie", "no mention here", d2);
        Tweet t3 = new Tweet(3, "ernie", "and also @oscar", d3);
        Map<String, Set<String>> followsGraph =
                SocialNetwork.guessFollowsGraph(Arrays.asList(t1, t2, t3));

        Set<String> follows = follows(followsGraph, "ernie");
        assertTrue("mention in the first tweet is kept", follows.contains("bert"));
        assertTrue("mention in the last tweet is kept", follows.contains("oscar"));
    }

    @Test
    public void testGuessFollowsGraphDifferentAuthors() {
        Tweet t1 = new Tweet(1, "ernie", "hello @bert", d1);
        Tweet t2 = new Tweet(2, "bert", "hi @oscar", d2);
        Map<String, Set<String>> followsGraph =
                SocialNetwork.guessFollowsGraph(Arrays.asList(t1, t2));

        assertTrue("ernie follows bert", follows(followsGraph, "ernie").contains("bert"));
        assertTrue("bert follows oscar", follows(followsGraph, "bert").contains("oscar"));
    }

    @Test
    public void testGuessFollowsGraphUpperCaseUsernames() {
        Tweet t1 = new Tweet(1, "Ernie", "hello @BERT", d1);
        Tweet t2 = new Tweet(2, "ERNIE", "again @Bert", d2);
        Map<String, Set<String>> followsGraph =
                SocialNetwork.guessFollowsGraph(Arrays.asList(t1, t2));

        assertEquals("case-insensitive: ernie follows bert exactly once",
                new HashSet<>(Arrays.asList("bert")), follows(followsGraph, "ernie"));
        assertEachUserOnce(followsGraph);
    }

    @Test
    public void testGuessFollowsGraphSelfMentionIsNotAFollow() {
        Tweet t = new Tweet(1, "ernie", "note to self @Ernie, and thanks @bert", d1);
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        assertFalse("the spec says users can't follow themselves",
                follows(followsGraph, "ernie").contains("ernie"));
        assertTrue("the mention of bert is still evidence", follows(followsGraph, "ernie").contains("bert"));
    }

    @Test
    public void testGuessFollowsGraphDoesNotModifyInput() {
        Tweet t1 = new Tweet(1, "ernie", "hello @bert", d1);
        Tweet t2 = new Tweet(2, "bert", "hi @oscar", d2);
        List<Tweet> input = new ArrayList<>(Arrays.asList(t1, t2));
        List<Tweet> copy = new ArrayList<>(input);

        SocialNetwork.guessFollowsGraph(input);

        assertEquals("the spec says the input list is not modified", copy, input);
    }

    // ---------- influencers() ----------

    @Test
    public void testInfluencersEmptyGraph() {
        List<String> influencers = SocialNetwork.influencers(new HashMap<String, Set<String>>());

        assertTrue("expected empty list", influencers.isEmpty());
    }

    @Test
    public void testInfluencersSingleUserWithoutFollowers() {
        Map<String, Set<String>> graph = graphOf("ernie", new String[] {});
        List<String> influencers = lower(SocialNetwork.influencers(graph));

        // the spec lists every user of the graph, so ernie may appear, but nobody else
        assertTrue("at most one user", influencers.size() <= 1);
        assertFalse("nobody except ernie can be listed", influencers.contains("bert"));
    }

    @Test
    public void testInfluencersSingleInfluencer() {
        Map<String, Set<String>> graph = graphOf("ernie", new String[] {"bert"});
        List<String> influencers = lower(SocialNetwork.influencers(graph));

        assertEquals("bert (1 follower) is before ernie (0)", "bert", influencers.get(0));
        assertTrue("ernie is a user of the graph too", influencers.contains("ernie"));
    }

    @Test
    public void testInfluencersMultipleInfluencers() {
        // followers: carol 3, dave 2, bert 1, others 0
        Map<String, Set<String>> graph = graphOf(
                "ernie", new String[] {"bert", "carol", "dave"},
                "oscar", new String[] {"carol", "dave"},
                "grover", new String[] {"carol"});
        List<String> influencers = lower(SocialNetwork.influencers(graph));

        assertEquals("all users of the graph, each once", 6, influencers.size());
        assertEquals("carol has 3 followers", "carol", influencers.get(0));
        assertEquals("dave has 2 followers", "dave", influencers.get(1));
        assertEquals("bert has 1 follower", "bert", influencers.get(2));
    }

    @Test
    public void testInfluencersTiedInfluence() {
        // bert and dave have 1 follower each, ernie and oscar have none
        Map<String, Set<String>> graph = graphOf(
                "ernie", new String[] {"bert"},
                "oscar", new String[] {"dave"});
        List<String> influencers = lower(SocialNetwork.influencers(graph));

        assertEquals("4 users", 4, influencers.size());
        // the order inside a tie is not specified: only compare the groups
        assertEquals("the two users with a follower come first",
                new HashSet<>(Arrays.asList("bert", "dave")),
                new HashSet<>(influencers.subList(0, 2)));
        assertEquals("the two users without followers come last",
                new HashSet<>(Arrays.asList("ernie", "oscar")),
                new HashSet<>(influencers.subList(2, 4)));
    }

    @Test
    public void testInfluencersTiedAtTheTop() {
        Map<String, Set<String>> graph = graphOf(
                "ernie", new String[] {"bert", "dave"},
                "oscar", new String[] {"bert", "dave", "carol"});
        List<String> influencers = lower(SocialNetwork.influencers(graph));

        assertEquals("bert and dave (2 followers each) lead",
                new HashSet<>(Arrays.asList("bert", "dave")),
                new HashSet<>(influencers.subList(0, 2)));
        assertEquals("carol (1 follower) is third", "carol", influencers.get(2));
    }

    @Test
    public void testInfluencersUpperCaseUsernames() {
        // "Bert" and "bert" are the same person; "Ernie" and "ERNIE" are the same follower
        Map<String, Set<String>> graph = graphOf(
                "Ernie", new String[] {"Bert"},
                "ERNIE", new String[] {"bert", "Oscar"},
                "grover", new String[] {"OSCAR"});
        List<String> influencers = lower(SocialNetwork.influencers(graph));

        assertEquals("distinct people only: ernie, grover, bert, oscar", 4, influencers.size());
        assertEquals("oscar has 2 followers (ernie, grover)", "oscar", influencers.get(0));
        assertEquals("bert has 1 follower (ernie, counted once)", "bert", influencers.get(1));
    }

    @Test
    public void testInfluencersDoesNotModifyInput() {
        Map<String, Set<String>> graph = graphOf("ernie", new String[] {"bert"});
        Map<String, Set<String>> copy = graphOf("ernie", new String[] {"bert"});

        SocialNetwork.influencers(graph);

        assertEquals("the spec says the graph is not modified", copy, graph);
    }

    @Test
    public void testGuessThenInfluencers() {
        Tweet t1 = new Tweet(1, "ernie", "hello @bert", d1);
        Tweet t2 = new Tweet(2, "oscar", "hi @Bert and @ernie", d2);
        Tweet t3 = new Tweet(3, "grover", "@BERT!", d3);
        List<String> influencers = lower(SocialNetwork.influencers(
                SocialNetwork.guessFollowsGraph(Arrays.asList(t1, t2, t3))));

        assertEquals("bert is mentioned by three different authors", "bert", influencers.get(0));
        assertEquals("ernie is mentioned by one author", "ernie", influencers.get(1));
    }
}
