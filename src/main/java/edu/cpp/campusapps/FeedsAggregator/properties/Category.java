package edu.cpp.campusapps.FeedsAggregator.properties;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private final List<String> groups = new ArrayList<>();
    private final List<String> feeds = new ArrayList<>();

    public List<String> getGroups() {
        return groups;
    }

    public List<String> getFeeds() {
        return feeds;
    }
}