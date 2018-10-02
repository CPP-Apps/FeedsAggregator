package edu.cpp.campusapps.FeedsAggregator.controller;

import com.rometools.rome.io.SyndFeedOutput;
import edu.cpp.campusapps.FeedsAggregator.service.AggregatedFeedService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableCaching
public class AggregatedFeedController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AggregatedFeedService service;

    @RequestMapping(name = "/", produces = "application/rss+xml")
    public String getAggregratedFeed(HttpServletRequest request) throws Exception {
        String categories = request.getParameter("categories");

        SyndFeedOutput output = new SyndFeedOutput();

        return output.outputString(service.aggregateFeeds(categories));
    }
}
