package edu.cpp.campusapps.FeedsAggregator.controller;

import com.rometools.rome.io.SyndFeedOutput;
import edu.cpp.campusapps.FeedsAggregator.service.AggregatedFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/v0")
public class AggregatedFeedControllerV0 {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AggregatedFeedService service;

    @RequestMapping(value = "/aggregated-feed", produces = "application/rss+xml")
    public String getAggregratedFeed(HttpServletRequest request) throws Exception {
        List<String> categories = Arrays.asList(request.getParameter("categories").split(","));

        SyndFeedOutput output = new SyndFeedOutput();

        return output.outputString(service.aggregateFeeds(categories));
    }

    @RequestMapping(value = "/personalized-feed", produces = "application/rss+xml")
    public String getPersonalizedFeed(HttpServletRequest request) throws Exception {
        SyndFeedOutput output = new SyndFeedOutput();

        return output.outputString(service.aggregateFeedsByGroups(request));
    }
}
