package edu.cpp.campusapps.FeedsAggregrator.controller;

import com.rometools.rome.io.SyndFeedOutput;
import edu.cpp.campusapps.FeedsAggregrator.Application;
import edu.cpp.campusapps.FeedsAggregrator.service.AggregatedFeedService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AggregatedFeedController {

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    AggregatedFeedService service;

    @RequestMapping("/")
    public String getAggregratedFeed(HttpServletRequest request) throws Exception {
        String categories = request.getParameter("categories");

        SyndFeedOutput output = new SyndFeedOutput();

        return output.outputString(service.aggregateFeeds(categories));
    }
}
