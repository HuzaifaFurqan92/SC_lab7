/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SocialNetwork provides methods that operate on a social network.
 * 
 * A social network is represented by a Map<String, Set<String>> where map[A] is
 * the set of people that person A follows on Twitter, and all people are
 * represented by their Twitter usernames. Users can't follow themselves. If A
 * doesn't follow anybody, then map[A] may be the empty set, or A may not even exist
 * as a key in the map; this is true even if A is followed by other people in the network.
 * Twitter usernames are not case sensitive, so "ernie" is the same as "ERNie".
 * A username should appear at most once as a key in the map or in any given
 * map[A] set.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class SocialNetwork {

    /** An @-mention: '@' (not directly after a username character) + username. */
    private static final Pattern MENTION =
            Pattern.compile("(?<![A-Za-z0-9_-])@([A-Za-z0-9_-]+)");

    /**
     * Guess who might follow whom, from evidence found in tweets.
     * 
     * @param tweets
     *            a list of tweets providing the evidence, not modified by this
     *            method.
     * @return a social network (as defined above) in which Ernie follows Bert
     *         if and only if there is evidence for it in the given list of
     *         tweets.
     *         One kind of evidence that Ernie follows Bert is if Ernie
     *         @-mentions Bert in a tweet. This must be implemented. Other kinds
     *         of evidence may be used at the implementor's discretion.
     *         All the Twitter usernames in the returned social network must be
     *         either authors or @-mentions in the list of tweets.
     */
    public static Map<String, Set<String>> guessFollowsGraph(List<Tweet> tweets) {
        // Implementation: the only evidence used is the @-mention. All names are
        // stored in lower case, so each user appears at most once.
        Map<String, Set<String>> followsGraph = new HashMap<>();
        for (Tweet tweet : tweets) {
            String author = tweet.getAuthor().toLowerCase();
            Matcher matcher = MENTION.matcher(tweet.getText());
            while (matcher.find()) {
                String mentioned = matcher.group(1).toLowerCase();
                if (!mentioned.equals(author)) { // users can't follow themselves
                    followsGraph.computeIfAbsent(author, k -> new HashSet<>()).add(mentioned);
                }
            }
        }
        return followsGraph;
    }

    /**
     * Find the people in a social network who have the greatest influence, in
     * the sense that they have the most followers.
     * 
     * @param followsGraph
     *            a social network (as defined above)
     * @return a list of all distinct Twitter usernames in followsGraph, in
     *         descending order of follower count.
     */
    public static List<String> influencers(Map<String, Set<String>> followsGraph) {
        // Implementation: names are returned in lower case; users with the same
        // follower count are in alphabetical order (the spec leaves this open).
        final Map<String, Set<String>> followers = new HashMap<>(); // user -> distinct followers
        for (Map.Entry<String, Set<String>> entry : followsGraph.entrySet()) {
            String follower = entry.getKey().toLowerCase();
            followers.computeIfAbsent(follower, k -> new HashSet<>());
            for (String name : entry.getValue()) {
                String followee = name.toLowerCase();
                followers.computeIfAbsent(followee, k -> new HashSet<>());
                if (!followee.equals(follower)) {
                    followers.get(followee).add(follower);
                }
            }
        }
        List<String> result = new ArrayList<>(followers.keySet());
        Collections.sort(result, (a, b) -> {
            int byCount = Integer.compare(followers.get(b).size(), followers.get(a).size());
            return byCount != 0 ? byCount : a.compareTo(b);
        });
        return result;
    }

}
