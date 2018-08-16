package edu.cpp.campusapps.FeedsAggregrator.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class FeedsProperties {

    private final Map<String, ArrayList<String>> feeds = new HashMap<>();

    public Map<String, ArrayList<String>> getFeeds() {
        return this.feeds;
    }
}
