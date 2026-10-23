/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main is a small driver to see Extract, Filter and SocialNetwork working on a
 * sample list of tweets. (The original starter code downloads live tweets; this
 * version uses a built-in sample so that it also runs offline. Main is not
 * used in grading.)
 */
public class Main {

    public static void main(String[] args) {
        List<Tweet> tweets = Arrays.asList(
            new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?",
                      Instant.parse("2016-02-17T10:00:00Z")),
            new Tweet(2, "bbitdiddle", "@Alyssa rivest talk in 30 minutes #hype",
                      Instant.parse("2016-02-17T11:00:00Z")),
            new Tweet(3, "carol", "thanks @bbitdiddle and @ALYSSA, mail me at carol@mit.edu",
                      Instant.parse("2016-02-17T12:30:00Z")),
            new Tweet(4, "Alyssa", "Talk about Rivest again tomorrow with @carol",
                      Instant.parse("2016-02-17T13:15:00Z")),
            new Tweet(5, "dave", "great talk @alyssa, see you @carol and @bbitdiddle",
                      Instant.parse("2016-02-17T14:00:00Z")),
            new Tweet(6, "erin", "following @alyssa and @dave closely",
                      Instant.parse("2016-02-17T15:00:00Z")),
            new Tweet(7, "frank", "no mention in this tweet",
                      Instant.parse("2016-02-17T16:00:00Z")));

        System.out.println("Number of tweets: " + tweets.size());

        Timespan span = Extract.getTimespan(tweets);
        System.out.println("Timespan: " + span.getStart() + " to " + span.getEnd());
        System.out.println("Mentioned users: "
                + new java.util.TreeSet<>(Extract.getMentionedUsers(tweets)));

        System.out.println("Written by alyssa: " + ids(Filter.writtenBy(tweets, "alyssa")));
        Timespan morning = new Timespan(Instant.parse("2016-02-17T09:00:00Z"),
                                        Instant.parse("2016-02-17T12:00:00Z"));
        System.out.println("In timespan 09:00-12:00: " + ids(Filter.inTimespan(tweets, morning)));
        System.out.println("Containing [talk, lunch]: "
                + ids(Filter.containing(tweets, Arrays.asList("talk", "lunch"))));

        // ---- Lab 07: social network ----
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);
        System.out.println();
        System.out.println("Guessed follows graph (user -> follows):");
        for (String user : new java.util.TreeSet<>(followsGraph.keySet())) {
            System.out.println("  " + user + " -> "
                    + new java.util.TreeSet<>(followsGraph.get(user)));
        }

        List<String> influencers = SocialNetwork.influencers(followsGraph);
        System.out.println();
        System.out.println("Top 10 most-followed people:");
        for (int i = 0; i < Math.min(10, influencers.size()); i++) {
            String user = influencers.get(i);
            int n = followerCount(followsGraph, user);
            System.out.println("  " + (i + 1) + ". " + user + " (" + n
                    + (n == 1 ? " follower)" : " followers)"));
        }
    }

    /** @return number of distinct people in the graph who follow the given user */
    private static int followerCount(Map<String, Set<String>> graph, String user) {
        int count = 0;
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            if (entry.getValue().contains(user)) {
                count++;
            }
        }
        return count;
    }

    /** @return the ids of the given tweets, for compact printing */
    private static String ids(List<Tweet> tweets) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tweets.size(); i++) {
            sb.append(i == 0 ? "" : ", ").append("tweet ").append(tweets.get(i).getId());
        }
        return sb.append("]").toString();
    }
}
