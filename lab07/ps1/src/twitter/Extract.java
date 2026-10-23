/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract consists of methods that extract information from a list of tweets.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Extract {

    /**
     * A mention is an '@' followed by a Twitter username. Usernames consist of
     * letters, digits, underscores and hyphens. The '@' must NOT be directly
     * preceded by a username character (so "bitdiddle@mit.edu" is not a
     * mention of "mit"), and the username ends at the first character that is
     * not a username character.
     */
    private static final Pattern MENTION =
            Pattern.compile("(?<![A-Za-z0-9_-])@([A-Za-z0-9_-]+)");

    /**
     * Get the time period spanned by tweets.
     * 
     * @param tweets list of tweets with distinct ids, not modified by this method.
     * @return a minimum-length time interval that contains the timestamp of
     *         every tweet in the list.
     *         Requires: tweets is not empty.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        if (tweets.isEmpty()) {
            throw new IllegalArgumentException("tweets must not be empty");
        }
        Instant earliest = tweets.get(0).getTimestamp();
        Instant latest = earliest;
        for (Tweet tweet : tweets) {
            Instant time = tweet.getTimestamp();
            if (time.isBefore(earliest)) {
                earliest = time;
            }
            if (time.isAfter(latest)) {
                latest = time;
            }
        }
        return new Timespan(earliest, latest);
    }

    /**
     * Get usernames mentioned in a list of tweets.
     * 
     * @param tweets list of tweets with distinct ids, not modified by this method.
     * @return the set of usernames who are mentioned in the text of the tweets.
     *         A username-mention is "@" followed by a Twitter username (as
     *         defined by Twitter's rules: letters, digits, underscores and
     *         hyphens), and the "@" must not be preceded by another username
     *         character. Twitter usernames are case-insensitive, and the returned
     *         set may include a username at most once. (This implementation
     *         returns every username in lower case.)
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        Set<String> mentioned = new HashSet<>();
        for (Tweet tweet : tweets) {
            Matcher matcher = MENTION.matcher(tweet.getText());
            while (matcher.find()) {
                mentioned.add(matcher.group(1).toLowerCase());
            }
        }
        return mentioned;
    }
}
